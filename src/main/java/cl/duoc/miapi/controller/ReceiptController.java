package cl.duoc.miapi.controller;

import cl.duoc.miapi.dto.ReceiptRequest;
import cl.duoc.miapi.dto.ReceiptResponse;
import cl.duoc.miapi.service.DateRange;
import cl.duoc.miapi.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
// Orígenes permitidos en application.properties (app.cors.allowed-origins)
@CrossOrigin(originPatterns = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/receipts")
@Tag(name = "Boletas", description = "Compras, historial y ventas")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ZoneId zone;

    public ReceiptController(ReceiptService receiptService,
                             @Value("${app.timezone}") String timezone) {
        this.receiptService = receiptService;
        this.zone = ZoneId.of(timezone);
    }

    // auth.getName() es el "sub" del JWT, o sea el correo del usuario conectado.
    // El usuario sale siempre del token, nunca del cuerpo: nadie puede comprar a nombre de otro.
    @Operation(summary = "Confirma la compra del carrito: descuenta stock y crea la boleta")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReceiptResponse create(@Valid @RequestBody ReceiptRequest request, Authentication auth) {
        return receiptService.create(auth.getName(), request);
    }

    @Operation(summary = "Historial de compras del usuario conectado")
    @GetMapping("/me")
    public List<ReceiptResponse> mine(Authentication auth) {
        return receiptService.findMine(auth.getName());
    }

    @Operation(summary = "Una boleta: el dueño, o Admin y Vendedor")
    @GetMapping("/{id}")
    public ReceiptResponse findById(@PathVariable Long id, Authentication auth) {
        return receiptService.findById(id, auth.getName(), isStaff(auth));
    }

    @Operation(summary = "Todas las ventas del período (Admin y Vendedor)",
            description = "Fechas en formato yyyy-MM-dd, ambas incluidas. Sin fechas: últimos 30 días.")
    @GetMapping
    public List<ReceiptResponse> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        DateRange range = DateRange.of(from, to, zone);
        return receiptService.findAll(range.start(), range.end());
    }

    private boolean isStaff(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SELLER"));
    }
}
