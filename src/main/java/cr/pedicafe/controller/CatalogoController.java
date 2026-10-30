package cr.pedicafe.controller;

import cr.pedicafe.domain.Producto;
import cr.pedicafe.service.CategoriaService;
import cr.pedicafe.service.FirebaseStorageService;
import cr.pedicafe.service.ProductoService;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CatalogoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final FirebaseStorageService firebaseStorageService;
    private final MessageSource messageSource;

    @GetMapping({"/", "/menu"})
    public String menu(Model model) {
        List<Producto> productos = productoService.getProductosActivos();
        prepararImagenes(productos);
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaService.getCategoriasActivas());
        return "menu";
    }

    @GetMapping("/menu/categoria/{idCategoria}")
    public String menuCategoria(@PathVariable Integer idCategoria, Model model) {
        List<Producto> productos = productoService.getProductosPorCategoria(idCategoria);
        prepararImagenes(productos);
        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaService.getCategoriasActivas());
        model.addAttribute("idCategoriaActual", idCategoria);
        return "menu";
    }

    @GetMapping("/menu/producto/{idProducto}")
    public String detalle(@PathVariable Integer idProducto, Model model,
                          RedirectAttributes atributos, Locale locale) {
        Optional<Producto> productoOptional = productoService.getProducto(idProducto);
        if (productoOptional.isEmpty()) {
            atributos.addFlashAttribute("error", messageSource.getMessage("product.notFound", null, locale));
            return "redirect:/menu";
        }
        var producto = productoOptional.get();
        if (!producto.isActivo()) {
            return "redirect:/menu";
        }
        producto.setImagenUrl(firebaseStorageService.getSignedImageUrl(producto.getImagenUrl()));
        model.addAttribute("producto", producto);
        return "producto/detalle";
    }

    private void prepararImagenes(List<Producto> productos) {
        for (Producto producto : productos) {
            producto.setImagenUrl(firebaseStorageService.getSignedImageUrl(producto.getImagenUrl()));
        }
    }
}
