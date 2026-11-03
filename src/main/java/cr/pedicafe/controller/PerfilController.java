package cr.pedicafe.controller;

import cr.pedicafe.service.UsuarioService;
import java.security.Principal;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    // http://localhost:8080/perfil
    
    @GetMapping
    public String perfil(Principal principal, Model model) {
        model.addAttribute("usuario",
                usuarioService.getUsuarioPorCorreo(principal.getName()).orElseThrow());
        return "usuario/perfil";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombre, Principal principal,
                          RedirectAttributes atributos, Locale locale) {
        try {
            usuarioService.actualizarNombre(principal.getName(), nombre);
            atributos.addFlashAttribute("mensaje",
                    messageSource.getMessage("user.profile.saved", null, locale));
        } catch (IllegalArgumentException ex) {
            atributos.addFlashAttribute("error",
                    messageSource.getMessage(ex.getMessage(), null, locale));
        }
        return "redirect:/perfil";
    }

    @PostMapping("/contrasena")
    public String cambiarContrasena(@RequestParam String contrasenaActual,
                                    @RequestParam String contrasenaNueva,
                                    @RequestParam String confirmarContrasena,
                                    Principal principal,
                                    RedirectAttributes atributos, Locale locale) {
        try {
            usuarioService.cambiarContrasena(principal.getName(),
                    contrasenaActual, contrasenaNueva, confirmarContrasena);
            atributos.addFlashAttribute("mensaje",
                    messageSource.getMessage("user.password.changed", null, locale));
        } catch (IllegalArgumentException ex) {
            atributos.addFlashAttribute("error",
                    messageSource.getMessage(ex.getMessage(), null, locale));
        }
        return "redirect:/perfil";
    }
}
