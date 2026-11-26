package cl.duoc.miapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Reglas y mensajes iguales a validateProduct de src/utils/administracion.ts del frontend.
// "Un producto nuevo debe tener al menos 1 unidad" se revisa en ProductService.create.
@Setter
@Getter
@Entity
public class Product {

    // Mínimo 3 caracteres (sin contar espacios al inicio/fin). Acepta letras, números,
    // espacios y los símbolos comunes en nombres de medicamentos: . , - / + % ( ) & '
    // Ej.: "Vetmedin 2.5mg", "Omega vet 3-6-9", "Amoxicilina + Ác. clavulánico (500mg)"
    public static final String NAME_REGEX =
            "^\\s*[\\p{L}\\p{N}.,\\-/+%()&'][\\p{L}\\p{N}.,\\-/+%()&' ]+[\\p{L}\\p{N}.,\\-/+%()&']\\s*$";
    public static final String NAME_MESSAGE =
            "Nombre y distribuidora: mínimo 3 caracteres. Solo letras, números, espacios y . , - / + % ( ) & '";

    // El id es el código visible del producto, el mismo que usa el frontend (ej. ME001).
    // No se genera solo: lo envía el cliente al crear el producto.
    @Id
    @Column(length = 20)
    private String id;

    @NotBlank(message = NAME_MESSAGE)
    @Pattern(regexp = NAME_REGEX, message = NAME_MESSAGE)
    private String name;

    @NotBlank(message = NAME_MESSAGE)
    @Pattern(regexp = NAME_REGEX, message = NAME_MESSAGE)
    @Column(nullable = false, length = 100)
    private String distributor;

    // Opcional; si no viene, el frontend muestra el principio activo
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
    @Column(length = 500)
    private String description;

    @NotNull(message = "El precio debe tener al menos 3 dígitos, sin símbolos ni decimales.")
    @Min(value = 100, message = "El precio debe tener al menos 3 dígitos, sin símbolos ni decimales.")
    private Integer price;

    @NotNull(message = "El stock debe ser un número entero mayor o igual a cero.")
    @Min(value = 0, message = "El stock debe ser un número entero mayor o igual a cero.")
    private Integer stock;

    private String image;

    private String activeIngredient;

    private String species;

    @NotNull(message = "Selecciona una categoría válida.")
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Product() {
    }

    public Product(String id, String name, String distributor, Integer price, Integer stock,
                   String image, String activeIngredient, String species,
                   Category category) {
        this.id = id;
        this.name = name;
        this.distributor = distributor;
        this.price = price;
        this.stock = stock;
        this.image = image;
        this.activeIngredient = activeIngredient;
        this.species = species;
        this.category = category;
    }
}
