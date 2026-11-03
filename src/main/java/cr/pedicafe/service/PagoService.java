package cr.pedicafe.service;

import cr.pedicafe.domain.Pago;
import cr.pedicafe.domain.Pedido;
import cr.pedicafe.repository.PagoRepository;
import cr.pedicafe.repository.PedidoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;

    public PagoService(PagoRepository pagoRepository,
            PedidoRepository pedidoRepository) {
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerTodos() {
        return pagoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerPorPedido(Integer idPedido) {
        return pagoRepository.findByPedido_IdPedido(idPedido);
    }

    @Transactional(readOnly = true)
    public List<Pago> obtenerPorEstado(String estado) {
        return pagoRepository.findByEstadoPago(estado);
    }

    @Transactional
    public Pago registrarPago(Integer idPedido, String metodoPago,
            Integer idUsuarioRegistra) {

        if (idPedido == null || metodoPago == null) {
            throw new IllegalArgumentException(
                    "Los datos del pago son obligatorios");
        }

        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pedido no encontrado"));

        boolean pagoExistente = !pagoRepository
                .findByPedido_IdPedido(idPedido)
                .isEmpty();

        if (pagoExistente) {
            throw new IllegalArgumentException(
                    "Este pedido ya tiene un registro de pago");
        }

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago(metodoPago);
        pago.setMonto(pedido.getTotal());
        pago.setIdUsuarioRegistra(idUsuarioRegistra);

        if ("TARJETA_SIMULADA".equals(metodoPago)) {
            pago.setEstadoPago("PAGADO");
            pago.setFechaPago(LocalDateTime.now());
            pago.setReferencia(UUID.randomUUID().toString());

        } else if ("MOSTRADOR".equals(metodoPago)) {
            pago.setEstadoPago("PENDIENTE");

        } else {
            throw new IllegalArgumentException(
                    "Método de pago inválido");
        }

        return pagoRepository.save(pago);
    }

    @Transactional
    public Pago confirmarPagoMostrador(Integer idPago,
            Integer idUsuarioRegistra) {

        if (idPago == null || idUsuarioRegistra == null) {
            throw new IllegalArgumentException(
                    "Los datos del pago son obligatorios");
        }

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pago no encontrado"));

        if (!"MOSTRADOR".equals(pago.getMetodoPago())) {
            throw new IllegalArgumentException(
                    "Este pago no corresponde a mostrador");
        }

        if (!"PENDIENTE".equals(pago.getEstadoPago())) {
            throw new IllegalArgumentException(
                    "El pago no está pendiente");
        }

        pago.setEstadoPago("PAGADO");
        pago.setFechaPago(LocalDateTime.now());
        pago.setIdUsuarioRegistra(idUsuarioRegistra);

        return pagoRepository.save(pago);
    }
}
