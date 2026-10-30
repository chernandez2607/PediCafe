package cr.pedicafe.repository;

import cr.pedicafe.domain.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    List<Producto> findByActivoTrueOrderByNombreAsc();
    List<Producto> findByCategoriaIdCategoriaAndActivoTrueOrderByNombreAsc(Integer idCategoria);
    boolean existsByCategoriaIdCategoriaAndActivoTrue(Integer idCategoria);
    boolean existsByCategoriaIdCategoria(Integer idCategoria);
}
