package cr.pedicafe.repository;

import cr.pedicafe.domain.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionSistemaRepository
        extends JpaRepository<ConfiguracionSistema, Byte> {
}