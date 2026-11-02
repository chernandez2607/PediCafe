package cr.pedicafe.controller;

import cr.pedicafe.domain.Usuario;
import cr.pedicafe.service.RolService;
import cr.pedicafe.service.UsuarioService;
import java.security.Principal;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final MessageSource messageSource;

    // http://localhost:8080/admin/usuarios
    @GetMapping
    public String listado(Model model) {
        model.addAttribute("usuarios", usuarioService.getUsuarios());
        return "usuario/listado";
    }

    @GetMapping("/editar/{idUsuario}")
    public String editar(@PathVariable Integer idUsuario, Model model,
                         RedirectAttributes atributos, Locale locale) {
        Optional<Usuario> usuarioOptional = usuarioService.getUsuario(idUsuario);
        if (usuarioOptional.isEmpty()) {
            atributos.addFlashAttribute("error",
                    messageSource.getMessage("user.notFound", null, locale));
            return "redirect:/admin/usuarios";
        }
        model.addAttribute("usuario", usuarioOptional.get());
        model.addAttribute("roles", rolService.getRoles());
        return "usuario/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Integer idUsuario,
                          @RequestParam Integer idRol,
                          @RequestParam(defaultValue = "false") boolean activo,
                          Principal principal,
                          RedirectAttributes atributos, Locale locale) {
        try {
            usuarioService.actualizarRolYEstado(idUsuario, idRol, activo, principal.getName());
            atributos.addFlashAttribute("mensaje",
                    messageSource.getMessage("user.saved", null, locale));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            atributos.addFlashAttribute("error",
                    messageSource.getMessage(ex.getMessage(), null, locale));
        }
        return "redirect:/admin/usuarios";
    }
}