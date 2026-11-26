package cl.duoc.miapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// Igual que normalizeCategory del frontend: solo letras y espacios, mínimo 3,
// y se guarda en MAYÚSCULAS (CategoryService la normaliza).
@Setter
@Getter
@Entity
public class Category {

    public static final String MESSAGE = "Categoría: mínimo 3 letras, sin números ni símbolos.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = MESSAGE)
    @Pattern(regexp = "^\\s*\\p{L}[\\p{L} ]+\\p{L}\\s*$", message = MESSAGE)
    @Column(nullable = false, unique = true)
    private String name;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }
}
