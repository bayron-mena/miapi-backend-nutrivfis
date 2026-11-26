package cl.duoc.miapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// Cuerpo de POST /api/v1/receipts: el carrito del frontend (CartItem: code, price, quantity)
public record ReceiptRequest(
        @NotEmpty(message = "El carrito está vacío o la compra ya fue confirmada.")
        @Size(max = 100, message = "El carrito tiene demasiados productos.")
        List<@Valid Item> items
) {
    public record Item(
            @NotBlank(message = "Falta el código del producto.")
            String productId,

            @NotNull(message = "Cantidad inválida en el carrito.")
            @Min(value = 1, message = "Cantidad inválida en el carrito.")
            Integer quantity,

            // Precio que vio el cliente. Si no coincide con el actual, la compra se rechaza
            // (igual que en el frontend: "El precio cambió...")
            @NotNull(message = "Falta el precio del producto.")
            Integer price
    ) {
    }
}
