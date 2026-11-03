package cr.pedicafe.service;

import cr.pedicafe.domain.Pedido;
import cr.pedicafe.domain.PedidoHistorialEstado;
import cr.pedicafe.repository.PedidoRepository;
import cr.pedicafe.repository.PedidoHistorialEstadoRepository;
import cr.pedicafe.repository.PagoRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaristaService {

    private static final ZoneId ZONA
            = ZoneId.of("America/Costa_Rica");

    private final PedidoRepository pedidoRepository;
    private final PedidoHistorialEstadoRepository historialRepository;
    private final PagoRepository pagoRepository;

    public BaristaService(
            PedidoRepository pedidoRepository,
            PedidoHistorialEstadoRepository historialRepository,
            PagoRepository pagoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.historialRepository = historialRepository;
        this.pagoRepository = pagoRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerColaPedidos() {

        List<String> estados = List.of(
                "RECIBIDO",
                "EN_PREPARACION",
                "LISTO"
        );

        return pedidoRepository
                .findByEstadoActualInOrderByFechaPedidoAsc(estados);
    }

    @Transactional(readOnly = true)
    public Pedido validarCodigoRetiro(String codigoRetiro) {

        if (codigoRetiro == null || codigoRetiro.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe ingresar un código de retiro");
        }

        Pedido pedido = pedidoRepository
                .findByCodigoRetiro(codigoRetiro)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró ningún pedido con ese código"));

        if (!"LISTO".equals(pedido.getEstadoActual())) {
            throw new IllegalArgumentException(
                    "El pedido todavía no está listo para retirar");
        }

        boolean pagoRealizado = pagoRepository
                .findByPedido_IdPedido(pedido.getIdPedido())
                .stream()
                .anyMatch(pago
                        -> "PAGADO".equals(pago.getEstadoPago()));

        if (!pagoRealizado) {
            throw new IllegalArgumentException(
                    "El pedido no tiene un pago confirmado");
        }

        return pedido;
    }

    @Transactional
    public Pedido actualizarEstado(
            Integer idPedido,
            String nuevoEstado,
            Integer idUsuario,
            String observacion) {

        if (idPedido == null || idUsuario == null) {
            throw new IllegalArgumentException(
                    "Los datos son obligatorios");
        }

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido no encontrado"));

        String estadoAnterior = pedido.getEstadoActual();

        boolean transicionValida
                = ("RECIBIDO".equals(estadoAnterior)
                && "EN_PREPARACION".equals(nuevoEstado))
                || ("EN_PREPARACION".equals(estadoAnterior)
                && "LISTO".equals(nuevoEstado))
                || ("LISTO".equals(estadoAnterior)
                && "ENTREGADO".equals(nuevoEstado));

        if (!transicionValida) {
            throw new IllegalArgumentException(
                    "No se permite este cambio de estado");
        }

        boolean pagoRealizado = pagoRepository
                .findByPedido_IdPedido(idPedido)
                .stream()
                .anyMatch(pago
                        -> "PAGADO".equals(pago.getEstadoPago()));

        if (!pagoRealizado) {
            throw new IllegalArgumentException(
                    "El pedido todavía no tiene un pago confirmado");
        }

        pedido.setEstadoActual(nuevoEstado);

        Pedido guardado = pedidoRepository.save(pedido);

        PedidoHistorialEstado historial
                = new PedidoHistorialEstado();

        historial.setPedido(guardado);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(nuevoEstado);
        historial.setFechaCambio(LocalDateTime.now(ZONA));
        historial.setIdUsuario(idUsuario);
        historial.setObservacion(observacion);

        historialRepository.save(historial);

        return guardado;
    }
}
