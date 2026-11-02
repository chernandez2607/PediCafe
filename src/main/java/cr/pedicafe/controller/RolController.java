package cr.pedicafe.controller;

import cr.pedicafe.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/roles")
public class RolController {

    private final RolService rolService;

    // http://localhost:8080/admin/roles
    @GetMapping
    public String listado(Model model) {
        model.addAttribute("roles", rolService.getRoles());
        model.addAttribute("cantidades", rolService.getCantidadUsuariosPorRol());
        return "rol/listado";
    }
}
