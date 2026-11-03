package cr.pedicafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPago")
    private Integer idPago;

    @ManyToOne
    @JoinColumn(name = "idPedido", nullable = false)
    private Pedido pedido;

    @Column(name = "metodo", nullable = false, length = 20)
    private String metodoPago;

    @Column(name = "estado", nullable = false, length = 15)
    private String estadoPago = "PENDIENTE";

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "fechaPago")
    private LocalDateTime fechaPago;

    @Column(name = "referenciaSimulada", length = 100)
    private String referencia;

    @Column(name = "idUsuarioRegistra")
    private Integer idUsuarioRegistra;
}
