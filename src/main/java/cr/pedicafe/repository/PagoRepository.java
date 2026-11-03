package cr.pedicafe.repository;

import cr.pedicafe.domain.Pago;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByPedido_IdPedido(Integer idPedido);

    List<Pago> findByEstadoPago(String estadoPago);
}
