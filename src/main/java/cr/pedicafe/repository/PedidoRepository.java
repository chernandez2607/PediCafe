package cr.pedicafe.repository;

import cr.pedicafe.domain.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository
        extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByIdUsuarioClienteOrderByFechaPedidoDesc(
            Integer idUsuarioCliente);

    Optional<Pedido> findByIdPedidoAndIdUsuarioCliente(
            Integer idPedido,
            Integer idUsuarioCliente);

    boolean existsByNumeroPedido(String numeroPedido);

    boolean existsByCodigoRetiro(String codigoRetiro);

    @Query(value = """
            SELECT u.idUsuario
            FROM usuario u
            JOIN rol r ON r.idRol = u.idRol
            WHERE u.correo = :correo
              AND u.activo = TRUE
              AND UPPER(r.nombre) = 'CLIENTE'
            """, nativeQuery = true)
    Optional<Integer> findIdClienteActivoPorCorreo(
            @Param("correo") String correo);
}