package cr.pedicafe.controller;

import cr.pedicafe.domain.Pedido;
import cr.pedicafe.service.BaristaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/barista")
public class BaristaController {

    private final BaristaService baristaService;

    public BaristaController(BaristaService baristaService) {
        this.baristaService = baristaService;
    }

    @GetMapping("/cola")
    public String cola(Model model) {
        model.addAttribute(
                "pedidos",
                baristaService.obtenerColaPedidos());

        return "barista/cola";
    }

    @PostMapping("/actualizar")
    public String actualizarEstado(
            @RequestParam Integer idPedido,
            @RequestParam String nuevoEstado,
            @RequestParam Integer idUsuario,
            @RequestParam(required = false) String observacion,
            RedirectAttributes redirectAttributes) {

        try {
            baristaService.actualizarEstado(
                    idPedido,
                    nuevoEstado,
                    idUsuario,
                    observacion);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Estado del pedido actualizado correctamente.");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/barista/cola";
    }

    @GetMapping("/validar-retiro")
    public String mostrarValidacion() {
        return "barista/validar-retiro";
    }

    @PostMapping("/validar-retiro")
    public String validarRetiro(
            @RequestParam String codigoRetiro,
            Model model) {

        try {
            Pedido pedido = baristaService.validarCodigoRetiro(
                    codigoRetiro);

            model.addAttribute("pedido", pedido);
            model.addAttribute(
                    "mensaje",
                    "Código válido. El pedido está listo para entregar.");

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "barista/validar-retiro";
    }

    @PostMapping("/entregar")
    public String entregarPedido(
            @RequestParam Integer idPedido,
            @RequestParam Integer idUsuario,
            RedirectAttributes redirectAttributes) {

        try {
            baristaService.actualizarEstado(
                    idPedido,
                    "ENTREGADO",
                    idUsuario,
                    "Pedido entregado al cliente mediante código de retiro");

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "¡Pedido entregado correctamente!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/barista/validar-retiro";
    }
}
