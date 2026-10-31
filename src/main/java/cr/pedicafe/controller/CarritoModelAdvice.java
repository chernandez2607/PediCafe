package cr.pedicafe.controller;

import cr.pedicafe.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class CarritoModelAdvice {

    private final CarritoService carritoService;

    @ModelAttribute("cantidadCarrito")
    public int cantidadCarrito() {
        return carritoService.getCantidadTotal();
    }
}