package cr.pedicafe.service;

import cr.pedicafe.domain.DetallePedido;
import cr.pedicafe.domain.FormularioPedido;
import cr.pedicafe.domain.ItemCarrito;
import cr.pedicafe.domain.Pedido;
import cr.pedicafe.domain.Producto;
import cr.pedicafe.domain.ResumenCarrito;
import cr.pedicafe.repository.DetallePedidoRepository;
import cr.pedicafe.repository.PedidoRepository;
import cr.pedicafe.repository.ProductoRepository;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private static final ZoneId ZONA =
            ZoneId.of("America/Costa_Rica");

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final ConfiguracionSistemaService configuracionSistemaService;

    @Value("${pedicafe.retiro.apertura:08:00}")
    private String apertura;

    @Value("${pedicafe.retiro.cierre:20:00}")
    private String cierre;

    public List<LocalDateTime> getHorariosRetiro() {
        LocalTime horaApertura = LocalTime.parse(apertura);
        LocalTime horaCierre = LocalTime.parse(cierre);

        if (!horaApertura.isBefore(horaCierre)) {
            throw new IllegalStateException("order.hours.invalid");
        }

        LocalDateTime ahora = LocalDateTime.now(ZONA);
        LocalDateTime minimo = ahora.plusMinutes(15);
        List<LocalDateTime> horarios = new ArrayList<>();

        // Ofrece turnos cada 15 minutos para los próximos 7 días.
        for (int i = 0; i < 7; i++) {
            LocalDate dia = ahora.toLocalDate().plusDays(i);
            LocalDateTime limite = dia.atTime(horaCierre);

            for (LocalDateTime turno = dia.atTime(horaApertura);
                    turno.isBefore(limite);
                    turno = turno.plusMinutes(15)) {

                if (!turno.isBefore(minimo)) {
                    horarios.add(turno);
                }
            }
        }

        return horarios;
    }

    @Transactional
    public Pedido crearPedido(
            Integer idUsuario,
            Map<Integer, Integer> cantidades,
            FormularioPedido formulario) {

        validarUsuario(idUsuario);

        if (cantidades == null || cantidades.isEmpty()) {
            throw new IllegalArgumentException("order.cart.empty");
        }

        if (formulario == null
                || formulario.getFechaRetiro() == null
                || !getHorariosRetiro().contains(
                        formulario.getFechaRetiro())) {

            throw new IllegalArgumentException("order.pickup.invalid");
        }

        String notas = formulario.getNotas();

        if (notas != null) {
            notas = notas.trim();

            if (notas.length() > 500) {
                throw new IllegalArgumentException("order.notes.max");
            }

            if (notas.isEmpty()) {
                notas = null;
            }
        }

        Map<Integer, Integer> copia =
                new LinkedHashMap<>(cantidades);

        for (Map.Entry<Integer, Integer> entrada : copia.entrySet()) {
            Integer idProducto = entrada.getKey();
            Integer cantidad = entrada.getValue();

            if (idProducto == null || idProducto < 1) {
                throw new IllegalArgumentException(
                        "cart.product.notFound");
            }

            if (cantidad == null || cantidad < 1 || cantidad > 20) {
                throw new IllegalArgumentException(
                        "cart.quantity.range");
            }
        }

        Map<Integer, Producto> productos = new LinkedHashMap<>();

        for (Producto producto
                : productoRepository.findAllById(copia.keySet())) {

            productos.put(producto.getIdProducto(), producto);
        }

        List<ItemCarrito> itemsValidados = new ArrayList<>();

        // Consulta los precios y disponibilidad en la base de datos.
        for (Map.Entry<Integer, Integer> entrada : copia.entrySet()) {
            Producto producto = productos.get(entrada.getKey());

            if (producto == null) {
                throw new IllegalArgumentException(
                        "cart.product.notFound");
            }

            if (!producto.isActivo()) {
                throw new IllegalArgumentException(
                        "cart.product.inactive");
            }

            if (producto.getPrecio() == null
                    || producto.getPrecio().signum() <= 0) {

                throw new IllegalArgumentException(
                        "order.product.priceInvalid");
            }

            itemsValidados.add(
                    new ItemCarrito(producto, entrada.getValue()));
        }

        ResumenCarrito resumen = new ResumenCarrito(
                itemsValidados,
                configuracionSistemaService.getImpuestoPorcentaje());

        Pedido pedido = new Pedido();

        pedido.setNumeroPedido(generarNumeroPedido());
        pedido.setCodigoRetiro(generarCodigoRetiro());
        pedido.setTipoPedido("ONLINE");
        pedido.setIdUsuarioCliente(idUsuario);
        pedido.setIdUsuarioRegistra(idUsuario);
        pedido.setFechaPedido(LocalDateTime.now(ZONA));
        pedido.setFechaRetiro(formulario.getFechaRetiro());
        pedido.setEstadoActual("RECIBIDO");
        pedido.setSubtotal(resumen.getSubtotal());
        pedido.setImpuestoPorcentaje(resumen.getImpuestoPorcentaje());
        pedido.setImpuestoMonto(resumen.getImpuestoMonto());
        pedido.setTotal(resumen.getTotal());
        pedido.setNotas(notas);

        Pedido guardado = pedidoRepository.save(pedido);

        List<DetallePedido> detalles = new ArrayList<>();

        for (ItemCarrito item : itemsValidados) {
            DetallePedido detalle = new DetallePedido();

            detalle.setPedido(guardado);
            detalle.setProducto(productos.get(item.getIdProducto()));
            detalle.setNombreProductoSnapshot(item.getNombre());
            detalle.setCantidad(item.getCantidad().shortValue());
            detalle.setPrecioUnitarioSnapshot(
                    item.getPrecioUnitario());
            detalle.setSubtotalLinea(
                    item.getSubtotal().setScale(
                            2, RoundingMode.HALF_UP));

            detalles.add(detalle);
        }

        detallePedidoRepository.saveAll(detalles);
        detallePedidoRepository.flush();

        return guardado;
    }

    @Transactional(readOnly = true)
    public List<Pedido> getPedidosCliente(Integer idUsuario) {
        validarUsuario(idUsuario);

        return pedidoRepository
                .findByIdUsuarioClienteOrderByFechaPedidoDesc(idUsuario);
    }

    @Transactional(readOnly = true)
    public Pedido getPedidoCliente(
            Integer idPedido, Integer idUsuario) {

        validarUsuario(idUsuario);

        if (idPedido == null || idPedido < 1) {
            throw new IllegalArgumentException("order.notFound");
        }

        return pedidoRepository
                .findByIdPedidoAndIdUsuarioCliente(idPedido, idUsuario)
                .orElseThrow(() ->
                        new IllegalArgumentException("order.notFound"));
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> getDetallesCliente(
            Integer idPedido, Integer idUsuario) {

        getPedidoCliente(idPedido, idUsuario);

        return detallePedidoRepository
                .findByPedidoIdPedidoOrderByIdDetallePedidoAsc(idPedido);
    }

    private void validarUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario < 1) {
            throw new IllegalStateException("order.login.required");
        }
    }

    private String generarNumeroPedido() {
        String numero;

        do {
            numero = "PC-" + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 24)
                    .toUpperCase(Locale.ROOT);

        } while (pedidoRepository.existsByNumeroPedido(numero));

        return numero;
    }

    private String generarCodigoRetiro() {
        String codigo;

        do {
            codigo = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase(Locale.ROOT);

        } while (pedidoRepository.existsByCodigoRetiro(codigo));

        return codigo;
    }
}
