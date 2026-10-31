package cr.pedicafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPedido")
    private Integer idPedido;

    @Column(name = "numeroPedido", nullable = false,
            unique = true, length = 30)
    private String numeroPedido;

    @Column(name = "tipoPedido", nullable = false, length = 12)
    private String tipoPedido = "ONLINE";

    @Column(name = "idUsuarioCliente")
    private Integer idUsuarioCliente;

    @Column(name = "idUsuarioRegistra", nullable = false)
    private Integer idUsuarioRegistra;

    @Column(name = "fechaPedido", nullable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "fechaRetiro")
    private LocalDateTime fechaRetiro;

    @Column(name = "codigoRetiro", nullable = false,
            unique = true, length = 20)
    private String codigoRetiro;

    @Column(name = "estadoActual", nullable = false, length = 20)
    private String estadoActual = "RECIBIDO";

    @Column(name = "subtotal", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "impuestoPorcentaje", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal impuestoPorcentaje;

    @Column(name = "impuestoMonto", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal impuestoMonto;

    @Column(name = "total", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "notas", length = 500)
    private String notas;
}