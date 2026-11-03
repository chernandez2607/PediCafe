package cr.pedicafe.controller;

import cr.pedicafe.domain.Pedido;
import cr.pedicafe.service.CajaService;
import cr.pedicafe.service.ProductoService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/caja")
public class CajaController {

    private final CajaService cajaService;
    private final ProductoService productoService;

    public CajaController(
            CajaService cajaService,
            ProductoService productoService) {
        this.cajaService = cajaService;
        this.productoService = productoService;
    }

    @GetMapping("/presencial")
    public String mostrarFormulario(Model model) {
        model.addAttribute(
                "productos",
                productoService.getProductosActivos());

        return "caja/presencial";
    }

    @PostMapping("/registrar")
    public String registrarPedido(
            @RequestParam Map<String, String> parametros,
            @RequestParam Integer idUsuarioRegistra,
            @RequestParam String metodoPago,
            @RequestParam(required = false) String notas,
            RedirectAttributes redirectAttributes) {

        try {
            Map<Integer, Integer> cantidades = new LinkedHashMap<>();

            for (Map.Entry<String, String> entrada : parametros.entrySet()) {
                if (entrada.getKey().startsWith("cantidad_")) {
                    Integer idProducto = Integer.valueOf(
                            entrada.getKey().substring(9));

                    Integer cantidad = Integer.valueOf(
                            entrada.getValue());

                    if (cantidad > 0) {
                        cantidades.put(idProducto, cantidad);
                    }
                }
            }

            Pedido pedido = cajaService.registrarPedidoPresencial(
                    cantidades,
                    notas,
                    idUsuarioRegistra,
                    metodoPago);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Pedido registrado correctamente. Número: "
                    + pedido.getNumeroPedido());

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/caja/presencial";
    }
}
