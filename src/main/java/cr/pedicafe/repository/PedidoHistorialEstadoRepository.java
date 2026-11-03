package cr.pedicafe.repository;

import cr.pedicafe.domain.PedidoHistorialEstado;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoHistorialEstadoRepository
        extends JpaRepository<PedidoHistorialEstado, Integer> {

    List<PedidoHistorialEstado>
            findByPedido_IdPedidoOrderByFechaCambioAsc(Integer idPedido);
}