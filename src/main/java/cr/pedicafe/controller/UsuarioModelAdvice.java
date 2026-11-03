package cr.pedicafe.controller;

import cr.pedicafe.domain.Rol;
import cr.pedicafe.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class UsuarioModelAdvice {

    private final UsuarioService usuarioService;

    @ModelAttribute
    public void agregarDatosDeSesion(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        model.addAttribute("autenticado", autenticado);
        model.addAttribute("esAdmin", autenticado && tieneRol(auth, Rol.ADMINISTRADOR));
        model.addAttribute("esBarista", autenticado && tieneRol(auth, Rol.BARISTA));
        model.addAttribute("esCajero", autenticado && tieneRol(auth, Rol.CAJERO));
        model.addAttribute("esCliente", autenticado && tieneRol(auth, Rol.CLIENTE));

        if (autenticado) {
            usuarioService.getUsuarioPorCorreo(auth.getName()).ifPresent(u -> {
                model.addAttribute("nombreUsuario", u.getNombre());
                model.addAttribute("rolUsuario", u.getRol().getNombre());
            });
        }
    }

    private boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol));
    }
}
