package cr.pedicafe.controller;

import cr.pedicafe.domain.Producto;
import cr.pedicafe.service.CategoriaService;
import cr.pedicafe.service.ProductoService;
import cr.pedicafe.service.ImagenStorageException;
import cr.pedicafe.service.FirebaseStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final FirebaseStorageService firebaseStorageService;
    private final MessageSource messageSource;

    @GetMapping
    public String listado(Model model) {
        List<Producto> productos = productoService.getProductos();
        prepararImagenes(productos);
        model.addAttribute("productos", productos);
        return "producto/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        prepararFormulario(model, new Producto());
        return "producto/formulario";
    }

    @GetMapping("/editar/{idProducto}")
    public String editar(@PathVariable Integer idProducto, Model model,
                         RedirectAttributes atributos, Locale locale) {
        Optional<Producto> productoOptional = productoService.getProducto(idProducto);
        if (productoOptional.isEmpty()) {
            atributos.addFlashAttribute("error", messageSource.getMessage("product.notFound", null, locale));
            return "redirect:/admin/productos";
        }
        prepararFormulario(model, productoOptional.get());
        return "producto/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Producto producto, BindingResult resultado,
                          @RequestParam("idCategoria") Integer idCategoria,
                          @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                          Model model, RedirectAttributes atributos, Locale locale) {
        if (resultado.hasErrors()) {
            prepararFormulario(model, producto);
            return "producto/formulario";
        }
        try {
            if (producto.getIdProducto() != null
                    && (imagenFile == null || imagenFile.isEmpty())
                    && (producto.getImagenUrl() == null || producto.getImagenUrl().isBlank())) {
                Optional<Producto> productoOptional = productoService.getProducto(producto.getIdProducto());
                if (productoOptional.isEmpty()) {
                    throw new IllegalArgumentException("product.notFound");
                }
                producto.setImagenUrl(productoOptional.get().getImagenUrl());
            }
            productoService.guardar(producto, idCategoria, imagenFile);
            atributos.addFlashAttribute("mensaje", messageSource.getMessage("product.saved", null, locale));
            return "redirect:/admin/productos";
        } catch (IllegalArgumentException | ImagenStorageException ex) {
            prepararFormulario(model, producto);
            String mensaje = ex.getMessage();
            model.addAttribute("error", messageSource.getMessage(mensaje, null, mensaje, locale));
            return "producto/formulario";
        }
    }

    @PostMapping("/desactivar/{idProducto}")
    public String desactivar(@PathVariable Integer idProducto, RedirectAttributes atributos, Locale locale) {
        try {
            productoService.desactivar(idProducto);
            atributos.addFlashAttribute("mensaje", messageSource.getMessage("product.deactivated", null, locale));
        } catch (IllegalArgumentException exception) {
            atributos.addFlashAttribute("error", messageSource.getMessage(exception.getMessage(), null, locale));
        }
        return "redirect:/admin/productos";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idProducto, RedirectAttributes atributos, Locale locale) {
        try {
            productoService.eliminar(idProducto);
            atributos.addFlashAttribute("mensaje", messageSource.getMessage("product.deleted", null, locale));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            atributos.addFlashAttribute("error", messageSource.getMessage(exception.getMessage(), null, locale));
        }
        return "redirect:/admin/productos";
    }

    private void prepararFormulario(Model model, Producto producto) {
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.getCategoriasActivas());
        model.addAttribute("imagenUrlVista",
                firebaseStorageService.getSignedImageUrl(producto.getImagenUrl()));
    }

    private void prepararImagenes(List<Producto> productos) {
        for (Producto producto : productos) {
            producto.setImagenUrl(firebaseStorageService.getSignedImageUrl(producto.getImagenUrl()));
        }
    }
}
