package cr.pedicafe.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.Getter;

@Getter
public class ResumenCarrito {

    private final BigDecimal subtotal;
    private final BigDecimal impuestoPorcentaje;
    private final BigDecimal impuestoMonto;
    private final BigDecimal total;
    private final int cantidadTotal;
    private final boolean vacio;
    private final boolean confirmable;

    public ResumenCarrito(List<ItemCarrito> items,
            BigDecimal impuestoPorcentaje) {

        if (impuestoPorcentaje == null
                || impuestoPorcentaje.signum() < 0) {
            throw new IllegalArgumentException(
                    "cart.tax.invalid");
        }

        BigDecimal suma = BigDecimal.ZERO;
        int cantidad = 0;
        boolean disponibles = !items.isEmpty();

        for (ItemCarrito item : items) {
            suma = suma.add(item.getSubtotal());
            cantidad += item.getCantidad();

            if (!item.isDisponible()) {
                disponibles = false;
            }
        }

        this.subtotal = suma.setScale(
                2, RoundingMode.HALF_UP);

        this.impuestoPorcentaje = impuestoPorcentaje;

        this.impuestoMonto = subtotal
                .multiply(impuestoPorcentaje)
                .divide(new BigDecimal("100"),
                        2, RoundingMode.HALF_UP);

        this.total = subtotal.add(impuestoMonto);
        this.cantidadTotal = cantidad;
        this.vacio = items.isEmpty();
        this.confirmable = disponibles;
    }
}
