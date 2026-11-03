package cr.pedicafe.service;

import cr.pedicafe.domain.DetallePedido;
import cr.pedicafe.domain.Pedido;
import cr.pedicafe.repository.DetallePedidoRepository;
import cr.pedicafe.repository.PedidoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;

    public DetallePedidoService(
            DetallePedidoRepository detallePedidoRepository,
            PedidoRepository pedidoRepository) {

        this.detallePedidoRepository = detallePedidoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPedido(Integer idPedido) {

        return pedidoRepository.findById(idPedido)
                .orElseThrow(() ->
                        new IllegalArgumentException("Pedido no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> obtenerDetalles(Integer idPedido) {

        if (!pedidoRepository.existsById(idPedido)) {
            throw new IllegalArgumentException("Pedido no encontrado");
        }

        return detallePedidoRepository
                .findByPedidoIdPedidoOrderByIdDetallePedidoAsc(idPedido);
    }
}

