package cr.pedicafe.repository;

import cr.pedicafe.domain.DetallePedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePedidoRepository
        extends JpaRepository<DetallePedido, Integer> {

    List<DetallePedido> findByPedidoIdPedidoOrderByIdDetallePedidoAsc(
            Integer idPedido);
}