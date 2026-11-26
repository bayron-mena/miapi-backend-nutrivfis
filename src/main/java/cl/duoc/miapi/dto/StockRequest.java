package cl.duoc.miapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Cuerpo de PATCH /api/v1/products/{id}/stock (igual que updateStock / validateStock del frontend)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockRequest {
    @NotNull(message = "El stock debe ser un número entero mayor o igual a cero.")
    @Min(value = 0, message = "El stock debe ser un número entero mayor o igual a cero.")
    private Integer stock;
}
