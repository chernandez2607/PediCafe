package cr.pedicafe.controller;

import cr.pedicafe.domain.ItemCarrito;
import cr.pedicafe.domain.ResumenCarrito;
import cr.pedicafe.service.CarritoService;
import cr.pedicafe.service.ConfiguracionSistemaService;
import java.util.List;
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
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final ConfiguracionSistemaService configuracionSistemaService;
    private final MessageSource messageSource;

    @GetMapping
    public String listado(Model model, Locale locale) {

        List<ItemCarrito> items = carritoService.getItems();
        model.addAttribute("items", items);

        if (!items.isEmpty()) {
            try {
                ResumenCarrito resumen = new ResumenCarrito(
                        items,
                        configuracionSistemaService
                                .getImpuestoPorcentaje());

                model.addAttribute("resumen", resumen);

            } catch (IllegalArgumentException
                    | IllegalStateException ex) {

                model.addAttribute("errorImpuesto",
                        texto(ex.getMessage(), locale));
            }
        }

        return "carrito/listado";
    }

    @PostMapping("/agregar")
    public String agregar(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam(value = "cantidad", defaultValue = "1")
                    Integer cantidad,
            RedirectAttributes atributos,
            Locale locale) {

        try {
            carritoService.agregar(idProducto, cantidad);

            atributos.addFlashAttribute("mensaje",
                    texto("cart.added", locale));

        } catch (IllegalArgumentException ex) {
            atributos.addFlashAttribute("error",
                    texto(ex.getMessage(), locale));
        }

        return "redirect:/carrito";
    }

    @PostMapping("/actualizar")
    public String actualizar(
            @RequestParam("idProducto") Integer idProducto,
            @RequestParam("cantidad") Integer cantidad,
            RedirectAttributes atributos,
            Locale locale) {

        try {
            carritoService.actualizar(idProducto, cantidad);

            atributos.addFlashAttribute("mensaje",
                    texto("cart.updated", locale));

        } catch (IllegalArgumentException ex) {
            atributos.addFlashAttribute("error",
                    texto(ex.getMessage(), locale));
        }

        return "redirect:/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminar(
            @RequestParam("idProducto") Integer idProducto,
            RedirectAttributes atributos,
            Locale locale) {

        carritoService.eliminar(idProducto);

        atributos.addFlashAttribute("mensaje",
                texto("cart.removed", locale));

        return "redirect:/carrito";
    }

    @PostMapping("/vaciar")
    public String vaciar(
            RedirectAttributes atributos,
            Locale locale) {

        carritoService.vaciar();

        atributos.addFlashAttribute("mensaje",
                texto("cart.cleared", locale));

        return "redirect:/carrito";
    }

    private String texto(String codigo, Locale locale) {
        return messageSource.getMessage(
                codigo, null, locale);
    }
}