package cr.pedicafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedidoHistorialEstado")
@Getter
@Setter
@NoArgsConstructor
public class PedidoHistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHistorial")
    private Integer idHistorial;

    @ManyToOne
    @JoinColumn(name = "idPedido", nullable = false)
    private Pedido pedido;

    @Column(name = "estadoAnterior", length = 20)
    private String estadoAnterior;

    @Column(name = "estadoNuevo", nullable = false, length = 20)
    private String estadoNuevo;

    @Column(name = "fechaCambio", nullable = false)
    private LocalDateTime fechaCambio;

    @Column(name = "idUsuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "observacion", length = 255)
    private String observacion;
}
