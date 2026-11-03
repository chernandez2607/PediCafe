package cr.pedicafe.controller;

import cr.pedicafe.service.DetallePedidoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/detalle-pedido")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    public DetallePedidoController(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }

    @GetMapping("/{idPedido}")
    public String detalle(
            @PathVariable Integer idPedido,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("pedido",
                    detallePedidoService.obtenerPedido(idPedido));

            model.addAttribute("detalles",
                    detallePedidoService.obtenerDetalles(idPedido));

            return "barista/detalle";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/barista/cola";
        }
    }
}