package cr.pedicafe.controller;

import cr.pedicafe.service.PagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("pagos", pagoService.obtenerTodos());
        return "pago/listado";
    }

    @PostMapping("/registrar")
    public String registrar(
            @RequestParam Integer idPedido,
            @RequestParam String metodoPago,
            @RequestParam(required = false) Integer idUsuarioRegistra,
            RedirectAttributes redirectAttributes) {

        try {
            pagoService.registrarPago(
                    idPedido, metodoPago, idUsuarioRegistra);

            redirectAttributes.addFlashAttribute(
                    "mensaje", "Pago registrado correctamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/pagos/listado";
    }

    @PostMapping("/confirmar")
    public String confirmarPago(
            @RequestParam Integer idPago,
            @RequestParam Integer idUsuarioRegistra,
            RedirectAttributes redirectAttributes) {

        try {
            pagoService.confirmarPagoMostrador(
                    idPago, idUsuarioRegistra);

            redirectAttributes.addFlashAttribute(
                    "mensaje", "Pago confirmado correctamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/pagos/listado";
    }
}