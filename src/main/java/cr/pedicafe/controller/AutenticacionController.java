package cr.pedicafe.controller;

import cr.pedicafe.domain.FormularioRegistro;
import cr.pedicafe.domain.Rol;
import cr.pedicafe.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AutenticacionController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    // http://localhost:8080/login
    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/redirigir";
        }
        return "auth/login";
    }

    // http://localhost:8080/registro
    @GetMapping("/registro")
    public String registro(Model model) {
        if (!model.containsAttribute("formularioRegistro")) {
            model.addAttribute("formularioRegistro", new FormularioRegistro());
        }
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(
            @Valid @ModelAttribute("formularioRegistro") FormularioRegistro formulario,
            BindingResult resultado,
            Model model,
            RedirectAttributes atributos,
            Locale locale) {

        if (resultado.hasErrors()) {
            return "auth/registro";
        }
        try {
            usuarioService.registrar(formulario);
            atributos.addFlashAttribute("mensaje",
                    messageSource.getMessage("user.registered", null, locale));
            return "redirect:/login";
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("error",
                    messageSource.getMessage(ex.getMessage(), null, locale));
            return "auth/registro";
        }
    }

    @GetMapping("/redirigir")
    public String redirigir(Authentication authentication) {
        boolean esCliente = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.CLIENTE));
        return esCliente ? "redirect:/menu" : "redirect:/admin";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "auth/acceso-denegado";
    }
}
