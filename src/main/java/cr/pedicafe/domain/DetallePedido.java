package cr.pedicafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detallePedido")
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDetallePedido")
    private Integer idDetallePedido;

    @ManyToOne
    @JoinColumn(name = "idPedido", nullable = false)
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "idProducto", nullable = false)
    private Producto producto;

    @Column(name = "nombreProductoSnapshot", nullable = false,
            length = 100)
    private String nombreProductoSnapshot;

    @Min(1)
    @Max(20)
    @Column(name = "cantidad", nullable = false)
    private Short cantidad;

    @Column(name = "precioUnitarioSnapshot", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal precioUnitarioSnapshot;

    @Column(name = "subtotalLinea", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal subtotalLinea;
}
