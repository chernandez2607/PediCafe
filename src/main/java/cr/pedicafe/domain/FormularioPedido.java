package cr.pedicafe.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class FormularioPedido {

    @NotNull(message = "{order.pickup.required}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaRetiro;

    @Size(max = 500, message = "{order.notes.max}")
    private String notas;
}
