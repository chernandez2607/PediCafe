package cr.pedicafe.service;

import cr.pedicafe.domain.ItemCarrito;
import cr.pedicafe.domain.Producto;
import cr.pedicafe.repository.ProductoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;
import cr.pedicafe.domain.FormularioPedido;
import cr.pedicafe.domain.Pedido;

@Service
@SessionScope
@RequiredArgsConstructor
public class CarritoService {

    private final ProductoRepository productoRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final PedidoService pedidoService;
    private final Map<Integer, ItemCarrito> items =
            new LinkedHashMap<>();

    @Transactional(readOnly = true)
    public synchronized void agregar(
            Integer idProducto, Integer cantidad) {

        validarCantidad(cantidad);

        Producto producto = obtenerProductoDisponible(idProducto);
        ItemCarrito existente = items.get(idProducto);

        int nuevaCantidad = cantidad;

        if (existente != null) {
            nuevaCantidad += existente.getCantidad();
        }

        validarCantidad(nuevaCantidad);

        items.put(idProducto,
                new ItemCarrito(producto, nuevaCantidad));
    }

    @Transactional(readOnly = true)
    public synchronized void actualizar(
            Integer idProducto, Integer cantidad) {

        validarCantidad(cantidad);

        if (!items.containsKey(idProducto)) {
            throw new IllegalArgumentException(
                    "cart.item.notFound");
        }

        Producto producto = obtenerProductoDisponible(idProducto);

        items.put(idProducto,
                new ItemCarrito(producto, cantidad));
    }

    public synchronized void eliminar(Integer idProducto) {
        items.remove(idProducto);
    }

    public synchronized void vaciar() {
        items.clear();
    }

    public synchronized boolean isVacio() {
        return items.isEmpty();
    }

    public synchronized int getCantidadTotal() {
        int cantidadTotal = 0;

        for (ItemCarrito item : items.values()) {
            cantidadTotal += item.getCantidad();
        }

        return cantidadTotal;
    }

    @Transactional(readOnly = true)
    public synchronized List<ItemCarrito> getItems() {

        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, Producto> productosPorId = new HashMap<>();

        for (Producto producto
                : productoRepository.findAllById(items.keySet())) {

            productosPorId.put(
                    producto.getIdProducto(), producto);
        }

        for (ItemCarrito item : items.values()) {
            Producto producto =
                    productosPorId.get(item.getIdProducto());

            if (producto == null) {
                item.setDisponible(false);
            } else {
                item.setNombre(producto.getNombre());
                item.setPrecioUnitario(producto.getPrecio());
                item.setDisponible(producto.isActivo());

                item.setImagenUrl(
                        firebaseStorageService.getSignedImageUrl(
                                producto.getImagenUrl()));
            }
        }

        return new ArrayList<>(items.values());
    }

    @Transactional(readOnly = true)
    public synchronized BigDecimal getSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemCarrito item : getItems()) {
            subtotal = subtotal.add(item.getSubtotal());
        }

        return subtotal;
    }

    private Producto obtenerProductoDisponible(
            Integer idProducto) {

        if (idProducto == null) {
            throw new IllegalArgumentException(
                    "cart.product.notFound");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException(
                        "cart.product.notFound"));

        if (!producto.isActivo()) {
            throw new IllegalArgumentException(
                    "cart.product.inactive");
        }

        return producto;
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1 || cantidad > 20) {
            throw new IllegalArgumentException(
                    "cart.quantity.range");
        }
    }
    public synchronized Map<Integer, Integer> getCantidades() {
    Map<Integer, Integer> cantidades = new LinkedHashMap<>();

    for (ItemCarrito item : items.values()) {
        cantidades.put(item.getIdProducto(), item.getCantidad());
    }

    return cantidades;
    }
    public synchronized Pedido confirmarPedido(
        Integer idUsuario,
        FormularioPedido formulario) {

    Map<Integer, Integer> cantidades = new LinkedHashMap<>();

    for (ItemCarrito item : items.values()) {
        cantidades.put(
                item.getIdProducto(),
                item.getCantidad());
    }

    Pedido pedido = pedidoService.crearPedido(
            idUsuario,
            cantidades,
            formulario);

    // Vacía el carrito después de guardar correctamente.
    items.clear();

    return pedido;
    }
}
