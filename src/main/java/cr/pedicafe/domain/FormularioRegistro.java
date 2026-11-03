package cr.pedicafe.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FormularioRegistro {

    @NotBlank(message = "{validation.user.name.required}")
    @Size(max = 100, message = "{validation.user.name.size}")
    private String nombre;

    @NotBlank(message = "{validation.user.email.required}")
    @Email(message = "{validation.user.email.invalid}")
    @Size(max = 150, message = "{validation.user.email.size}")
    private String correo;

    @NotBlank(message = "{validation.user.password.required}")
    @Size(min = 8, max = 72, message = "{validation.user.password.size}")
    private String contrasena;

    @NotBlank(message = "{validation.user.passwordConfirm.required}")
    private String confirmarContrasena;
}
