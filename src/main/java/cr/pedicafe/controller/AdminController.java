package cr.pedicafe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
public class AdminController {

    // http://localhost:8080/admin
    @GetMapping
    public String panel() {
        return "admin/panel";
    }

    @GetMapping("/inventario")
    public String inventario() {
        return "admin/inventario";
    }

    @GetMapping("/recetas")
    public String recetas() {
        return "admin/recetas";
    }

    @GetMapping("/reportes")
    public String reportes() {
        return "admin/reportes";
    }
}