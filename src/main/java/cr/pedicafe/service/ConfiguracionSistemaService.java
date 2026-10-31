package cr.pedicafe.service;

import cr.pedicafe.domain.ConfiguracionSistema;
import cr.pedicafe.repository.ConfiguracionSistemaRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracionSistemaService {

    private final ConfiguracionSistemaRepository
            configuracionSistemaRepository;

    @Transactional(readOnly = true)
    public BigDecimal getImpuestoPorcentaje() {

        ConfiguracionSistema configuracion =
                configuracionSistemaRepository.findById((byte) 1)
                        .orElseThrow(() -> new IllegalStateException(
                                "cart.tax.notConfigured"));

        BigDecimal porcentaje =
                configuracion.getImpuestoPorcentaje();

        if (porcentaje == null || porcentaje.signum() < 0) {
            throw new IllegalStateException(
                    "cart.tax.invalid");
        }

        return porcentaje;
    }
}