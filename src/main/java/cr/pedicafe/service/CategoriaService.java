package cr.pedicafe.service;

import cr.pedicafe.domain.Categoria;
import cr.pedicafe.repository.CategoriaRepository;
import cr.pedicafe.repository.ProductoRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<Categoria> getCategorias() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Categoria> getCategoriasActivas() {
        return categoriaRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<Categoria> getCategoria(Integer idCategoria) {
        return categoriaRepository.findById(idCategoria);
    }

    @Transactional
    public void guardar(Categoria categoria) {
        categoria.setNombre(categoria.getNombre().trim());
        boolean duplicada = categoria.getIdCategoria() == null
                ? categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())
                : categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot(
                        categoria.getNombre(), categoria.getIdCategoria());
        if (duplicada) {
            throw new IllegalArgumentException("category.name.duplicate");
        }
        if (categoria.getIdCategoria() != null) {
            Optional<Categoria> categoriaOptional = getCategoria(categoria.getIdCategoria());
            if (categoriaOptional.isEmpty()) {
                throw new IllegalArgumentException("category.notFound");
            }
            Categoria existente = categoriaOptional.get();
            if (existente.isActivo() && !categoria.isActivo()
                    && productoRepository.existsByCategoriaIdCategoriaAndActivoTrue(categoria.getIdCategoria())) {
                throw new IllegalStateException("category.deactivate.products");
            }
            existente.setNombre(categoria.getNombre().trim());
            existente.setDescripcion(categoria.getDescripcion());
            existente.setActivo(categoria.isActivo());
            categoriaRepository.save(existente);
        } else {
            categoriaRepository.save(categoria);
        }
    }

    @Transactional
    public void desactivar(Integer idCategoria) {
        Optional<Categoria> categoriaOptional = getCategoria(idCategoria);
        if (categoriaOptional.isEmpty()) {
            throw new IllegalArgumentException("category.notFound");
        }
        Categoria categoria = categoriaOptional.get();
        if (productoRepository.existsByCategoriaIdCategoriaAndActivoTrue(idCategoria)) {
            throw new IllegalStateException("category.deactivate.products");
        }
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Integer idCategoria) {
        if (!categoriaRepository.existsById(idCategoria)) {
            throw new IllegalArgumentException("category.notFound");
        }
        if (productoRepository.existsByCategoriaIdCategoria(idCategoria)) {
            throw new IllegalStateException(
                    "category.delete.products");
        }
        try {
            categoriaRepository.deleteById(idCategoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException(
                    "category.delete.related", exception);
        }
    }
}
