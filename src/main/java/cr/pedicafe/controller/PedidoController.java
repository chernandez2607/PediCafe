package cr.pedicafe.controller;

import cr.pedicafe.domain.DetallePedido;
import cr.pedicafe.domain.FormularioPedido;
import cr.pedicafe.domain.ItemCarrito;
import cr.pedicafe.domain.Pedido;
import cr.pedicafe.domain.ResumenCarrito;
import cr.pedicafe.service.CarritoService;
import cr.pedicafe.service.ConfiguracionSistemaService;
import cr.pedicafe.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.Serializable;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {

    private static final String TOKEN = "pc.pedido.token";
    private static final String ULTIMO_PEDIDO = "pc.pedido.ultimo";

    private final CarritoService carritoService;
    private final PedidoService pedidoService;
    private final ConfiguracionSistemaService configuracionSistemaService;
    private final MessageSource messageSource;

    private record Confirmacion(
            String token,
            Integer idUsuario,
            Integer idPedido) implements Serializable {
    }

    @GetMapping("/confirmar")
    public String mostrarConfirmacion(
            Model model,
            HttpSession sesion,
            RedirectAttributes redirect,
            Locale locale) {

        if (!model.containsAttribute("formularioPedido")) {
            model.addAttribute(
                    "formularioPedido",
                    new FormularioPedido());
        }

        try {
            cargarConfirmacion(model, sesion);
            return "pedido/confirmacion";

        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute(
                    "error", texto(ex.getMessage(), locale));

            return "redirect:/carrito";
        }
    }

    @PostMapping("/confirmar")
    public String confirmarPedido(
            @Valid @ModelAttribute("formularioPedido")
            FormularioPedido formulario,
            BindingResult validacion,
            @RequestParam(value = "tokenPedido", required = false)
            String token,
            Principal principal,
            HttpSession sesion,
            Model model,
            RedirectAttributes redirect,
            Locale locale) {

        try {
            Integer idUsuario =
                    pedidoService.getIdClienteAutenticado(principal);

            if (idUsuario == null) {
                throw new IllegalStateException(
                        "order.login.required");
            }

            synchronized (sesion) {
                // Si se reenvía una confirmación ya guardada,
                // abre el resultado sin crear otro pedido.
                Object anterior = sesion.getAttribute(ULTIMO_PEDIDO);

                if (anterior instanceof Confirmacion confirmacion
                        && confirmacion.token().equals(token)
                        && confirmacion.idUsuario().equals(idUsuario)) {

                    return "redirect:/pedidos/"
                            + confirmacion.idPedido()
                            + "/resultado";
                }

                if (token == null
                        || !token.equals(sesion.getAttribute(TOKEN))) {

                    throw new IllegalStateException(
                            "order.confirmation.expired");
                }

                if (validacion.hasErrors()) {
                    cargarConfirmacion(model, sesion);
                    return "pedido/confirmacion";
                }

                Pedido pedido = carritoService.confirmarPedido(
                        idUsuario, formulario);

                sesion.setAttribute(
                        ULTIMO_PEDIDO,
                        new Confirmacion(
                                token,
                                idUsuario,
                                pedido.getIdPedido()));

                sesion.removeAttribute(TOKEN);

                return "redirect:/pedidos/"
                        + pedido.getIdPedido()
                        + "/resultado";
            }

        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirect.addFlashAttribute(
                    "error", texto(ex.getMessage(), locale));

        } catch (DataAccessException | TransactionException ex) {
            log.error("No fue posible guardar el pedido.", ex);

            redirect.addFlashAttribute(
                    "error", texto("order.save.failed", locale));
        }

        redirect.addFlashAttribute(
                "formularioPedido", formulario);

        return "redirect:/pedidos/confirmar";
    }

    @GetMapping("/{idPedido}/resultado")
    public String resultado(
            @PathVariable("idPedido") Integer idPedido,
            Principal principal,
            Model model,
            RedirectAttributes redirect,
            Locale locale) {

        return cargarPedido(
                idPedido, principal, model,
                redirect, locale, true);
    }

    @GetMapping("/{idPedido}/seguimiento")
    public String seguimiento(
            @PathVariable("idPedido") Integer idPedido,
            Principal principal,
            Model model,
            RedirectAttributes redirect,
            Locale locale) {

        return cargarPedido(
                idPedido, principal, model,
                redirect, locale, false);
    }

    @GetMapping("/historial")
    public String historial(
            Principal principal,
            Model model,
            RedirectAttributes redirect,
            Locale locale) {

        Integer idUsuario =
                pedidoService.getIdClienteAutenticado(principal);

        if (idUsuario == null) {
            redirect.addFlashAttribute(
                    "error", texto("order.login.required", locale));

            return "redirect:/carrito";
        }

        List<Pedido> pedidos =
                pedidoService.getPedidosCliente(idUsuario);

        Map<Integer, List<DetallePedido>> detallesPorPedido =
                new LinkedHashMap<>();

        for (Pedido pedido : pedidos) {
            detallesPorPedido.put(
                    pedido.getIdPedido(),
                    pedidoService.getDetallesCliente(
                            pedido.getIdPedido(), idUsuario));
        }

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("detallesPorPedido", detallesPorPedido);

        return "pedido/historial";
    }

    private void cargarConfirmacion(
            Model model, HttpSession sesion) {

        List<ItemCarrito> items = carritoService.getItems();

        if (items.isEmpty()) {
            throw new IllegalArgumentException("order.cart.empty");
        }

        ResumenCarrito resumen = new ResumenCarrito(
                items,
                configuracionSistemaService.getImpuestoPorcentaje());

        if (!resumen.isConfirmable()) {
            throw new IllegalArgumentException(
                    "cart.unavailableWarning");
        }

        model.addAttribute("items", items);
        model.addAttribute("resumen", resumen);
        model.addAttribute(
                "horariosRetiro",
                pedidoService.getHorariosRetiro());

        synchronized (sesion) {
            Object token = sesion.getAttribute(TOKEN);

            if (!(token instanceof String)) {
                token = UUID.randomUUID().toString();
                sesion.setAttribute(TOKEN, token);
            }

            model.addAttribute("tokenPedido", token);
        }
    }

    private String cargarPedido(
            Integer idPedido,
            Principal principal,
            Model model,
            RedirectAttributes redirect,
            Locale locale,
            boolean esResultado) {

        Integer idUsuario =
                pedidoService.getIdClienteAutenticado(principal);

        if (idUsuario == null) {
            redirect.addFlashAttribute(
                    "error", texto("order.login.required", locale));

            return "redirect:/carrito";
        }

        try {
            Pedido pedido = pedidoService.getPedidoCliente(
                    idPedido, idUsuario);

            List<DetallePedido> detalles =
                    pedidoService.getDetallesCliente(
                            idPedido, idUsuario);

            model.addAttribute("pedido", pedido);
            model.addAttribute("detalles", detalles);
            model.addAttribute("esResultado", esResultado);

            return "pedido/detalle";

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private String texto(String clave, Locale locale) {
        return messageSource.getMessage(
                clave == null ? "order.operation.failed" : clave,
                null,
                "No fue posible completar la operación.",
                locale);
    }
}