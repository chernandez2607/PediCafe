package cr.pedicafe.repository;

import cr.pedicafe.domain.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    
    boolean existsByCorreoIgnoreCase(String correo);

    List<Usuario> findAllByOrderByNombreAsc();

    long countByRolIdRol(Integer idRol);
}
