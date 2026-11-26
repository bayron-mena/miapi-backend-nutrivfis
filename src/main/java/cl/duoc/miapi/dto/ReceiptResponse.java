package cl.duoc.miapi.dto;

import cl.duoc.miapi.model.Receipt;
import cl.duoc.miapi.model.ReceiptDetail;

import java.time.Instant;
import java.util.List;

// Boleta tal como la ve el frontend. No se devuelve la entidad para no exponer
// datos del usuario (como el hash de la contraseña).
public record ReceiptResponse(
        Long id,
        // Número visible de la boleta, ej. "B-000042"
        String number,
        Instant createdAt,
        String customerName,
        String customerEmail,
        int netAmount,
        int taxAmount,
        int total,
        int itemCount,
        List<Detail> details
) {
    public record Detail(
            String productId,
            String productName,
            String categoryName,
            int unitPrice,
            int quantity,
            int subtotal
    ) {
        static Detail from(ReceiptDetail d) {
            return new Detail(d.getProductId(), d.getProductName(), d.getCategoryName(),
                    d.getUnitPrice(), d.getQuantity(), d.getSubtotal());
        }
    }

    public static ReceiptResponse from(Receipt r) {
        List<Detail> details = r.getDetails().stream().map(Detail::from).toList();
        return new ReceiptResponse(
                r.getId(),
                "B-%06d".formatted(r.getId()),
                r.getCreatedAt(),
                r.getCustomerName(),
                r.getCustomerEmail(),
                r.getNetAmount(),
                r.getTaxAmount(),
                r.getTotal(),
                details.stream().mapToInt(Detail::quantity).sum(),
                details);
    }
}
