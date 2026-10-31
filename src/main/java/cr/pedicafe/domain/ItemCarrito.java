package cr.pedicafe.domain;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemCarrito {

    private Integer idProducto;
    private String nombre;
    private String imagenUrl;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private boolean disponible;

    public ItemCarrito(Producto producto, Integer cantidad) {
        this.idProducto = producto.getIdProducto();
        this.nombre = producto.getNombre();
        this.imagenUrl = producto.getImagenUrl();
        this.precioUnitario = producto.getPrecio();
        this.cantidad = cantidad;
        this.disponible = producto.isActivo();
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }
}