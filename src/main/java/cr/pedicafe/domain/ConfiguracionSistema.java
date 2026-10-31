package cr.pedicafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "configuracionSistema")
@Getter
@Setter
@NoArgsConstructor
public class ConfiguracionSistema {

    @Id
    @Column(name = "idConfiguracion")
    private Byte idConfiguracion;

    @Column(name = "impuestoPorcentaje", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal impuestoPorcentaje;
}