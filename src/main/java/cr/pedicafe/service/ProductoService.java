package cr.pedicafe.service;

import cr.pedicafe.domain.Categoria;
import cr.pedicafe.domain.Producto;
import cr.pedicafe.repository.CategoriaRepository;
import cr.pedicafe.repository.ProductoRepository;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FirebaseStorageService firebaseStorageService;

    @Transactional(readOnly = true)
    public List<Producto> getProductosActivos() {
        return productoRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Producto> getProductos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Producto> getProductosPorCategoria(Integer idCategoria) {
        return productoRepository.findByCategoriaIdCategoriaAndActivoTrueOrderByNombreAsc(idCategoria);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> getProducto(Integer idProducto) {
        return productoRepository.findById(idProducto);
    }

    @Transactional
    public void guardar(Producto producto, Integer idCategoria, MultipartFile imagenFile) {
        if (idCategoria == null) {
            throw new IllegalArgumentException("product.category.required");
        }
        if (producto.getPrecio() == null || producto.getPrecio().signum() <= 0) {
            throw new IllegalArgumentException("product.price.positive");
        }
        Optional<Categoria> categoriaOptional = categoriaRepository.findById(idCategoria);
        if (categoriaOptional.isEmpty()) {
            throw new IllegalArgumentException("product.category.notFound");
        }
        Categoria categoria = categoriaOptional.get();
        if (!categoria.isActivo()) {
            throw new IllegalArgumentException("product.category.inactive");
        }
        Producto existente = null;
        if (producto.getIdProducto() != null) {
            Optional<Producto> productoOptional = getProducto(producto.getIdProducto());
            if (productoOptional.isEmpty()) {
                throw new IllegalArgumentException("product.notFound");
            }
            existente = productoOptional.get();
        }
        String imagenUrl = resolverImagenUrl(producto, existente, imagenFile);

        producto.setCategoria(categoria);
        producto.setNombre(producto.getNombre().trim());
        producto.setImagenUrl(imagenUrl);
        if (existente != null) {
            existente.setNombre(producto.getNombre());
            existente.setDescripcion(producto.getDescripcion());
            existente.setPrecio(producto.getPrecio());
            existente.setImagenUrl(imagenUrl);
            existente.setCategoria(categoria);
            existente.setActivo(producto.isActivo());
            productoRepository.save(existente);
        } else {
            productoRepository.save(producto);
        }
    }

    private String resolverImagenUrl(Producto producto, Producto existente, MultipartFile imagenFile) {
        if (imagenFile != null && !imagenFile.isEmpty()) {
            return firebaseStorageService.uploadImage(imagenFile);
        }

        String imagenManual = producto.getImagenUrl() == null ? "" : producto.getImagenUrl().trim();
        String imagenActual = existente == null ? null : existente.getImagenUrl();
        if (!imagenManual.isEmpty() && (imagenActual == null || !imagenManual.equals(imagenActual))) {
            validarUrlManual(imagenManual);
            return imagenManual;
        }
        if (imagenActual != null && !imagenActual.isBlank()) {
            return imagenActual;
        }
        throw new IllegalArgumentException("product.image.required");
    }

    private void validarUrlManual(String valor) {
        try {
            URI uri = URI.create(valor);
            String esquema = uri.getScheme();
            if (!uri.isAbsolute() || uri.getHost() == null
                    || !("http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema))) {
                throw new IllegalArgumentException("product.image.url.scheme");
            }
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().startsWith("product.image.url")) {
                throw ex;
            }
            throw new IllegalArgumentException("product.image.url.invalid");
        }
    }

    @Transactional
    public void desactivar(Integer idProducto) {
        Optional<Producto> productoOptional = getProducto(idProducto);
        if (productoOptional.isEmpty()) {
            throw new IllegalArgumentException("product.notFound");
        }
        Producto producto = productoOptional.get();
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Integer idProducto) {
        if (!productoRepository.existsById(idProducto)) {
            throw new IllegalArgumentException("product.notFound");
        }
        try {
            productoRepository.deleteById(idProducto);
            productoRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalStateException(
                    "product.delete.references",
                    exception);
        }
    }
}
