package cr.pedicafe.service;

import cr.pedicafe.domain.Pedido;
import cr.pedicafe.domain.Pago;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CajaService {

    private final PedidoService pedidoService;
    private final PagoService pagoService;

    public CajaService(
            PedidoService pedidoService,
            PagoService pagoService) {
        this.pedidoService = pedidoService;
        this.pagoService = pagoService;
    }

    @Transactional
    public Pedido registrarPedidoPresencial(
            Map<Integer, Integer> cantidades,
            String notas,
            Integer idUsuarioRegistra,
            String metodoPago) {

        Pedido pedido = pedidoService.crearPedidoPresencial(
                cantidades,
                notas,
                idUsuarioRegistra);

        Pago pago = pagoService.registrarPago(
                pedido.getIdPedido(),
                metodoPago,
                idUsuarioRegistra);

        return pedido;
    }
}
