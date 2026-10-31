package cr.pedicafe.repository;

import cr.pedicafe.domain.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByIdUsuarioClienteOrderByFechaPedidoDesc(
            Integer idUsuarioCliente);

    Optional<Pedido> findByIdPedidoAndIdUsuarioCliente(
            Integer idPedido, Integer idUsuarioCliente);

    boolean existsByNumeroPedido(String numeroPedido);

    boolean existsByCodigoRetiro(String codigoRetiro);
}