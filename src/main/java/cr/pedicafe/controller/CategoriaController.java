package cr.pedicafe.controller;

import cr.pedicafe.domain.Categoria;
import cr.pedicafe.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final MessageSource messageSource;

    @GetMapping
    public String listado(Model model) {
        model.addAttribute("categorias", categoriaService.getCategorias());
        return "categoria/listado";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categoria/formulario";
    }

    @GetMapping("/editar/{idCategoria}")
    public String editar(@PathVariable Integer idCategoria, Model model,
                         RedirectAttributes atributos, Locale locale) {
        Optional<Categoria> categoriaOptional = categoriaService.getCategoria(idCategoria);
        if (categoriaOptional.isEmpty()) {
            atributos.addFlashAttribute("error", messageSource.getMessage("category.notFound", null, locale));
            return "redirect:/admin/categorias";
        }
        model.addAttribute("categoria", categoriaOptional.get());
        return "categoria/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid Categoria categoria, BindingResult resultado, RedirectAttributes atributos,
                          Model model, Locale locale) {
        if (resultado.hasErrors()) {
            return "categoria/formulario";
        }
        try {
            categoriaService.guardar(categoria);
            atributos.addFlashAttribute("mensaje", messageSource.getMessage("category.saved", null, locale));
            return "redirect:/admin/categorias";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", messageSource.getMessage(ex.getMessage(), null, locale));
            return "categoria/formulario";
        } catch (IllegalStateException ex) {
            model.addAttribute("error", messageSource.getMessage(ex.getMessage(), null, locale));
            return "categoria/formulario";
        }
    }

    @PostMapping("/desactivar/{idCategoria}")
    public String desactivar(@PathVariable Integer idCategoria, RedirectAttributes atributos, Locale locale) {
        try {
            categoriaService.desactivar(idCategoria);
            atributos.addFlashAttribute("mensaje", messageSource.getMessage("category.deactivated", null, locale));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            atributos.addFlashAttribute("error", messageSource.getMessage(exception.getMessage(), null, locale));
        }
        return "redirect:/admin/categorias";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idCategoria, RedirectAttributes atributos, Locale locale) {
        try {
            categoriaService.eliminar(idCategoria);
            atributos.addFlashAttribute("mensaje", messageSource.getMessage("category.deleted", null, locale));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            atributos.addFlashAttribute("error", messageSource.getMessage(exception.getMessage(), null, locale));
        }
        return "redirect:/admin/categorias";
    }
}
