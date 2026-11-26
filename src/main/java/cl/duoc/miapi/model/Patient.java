package cl.duoc.miapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "La especie es obligatoria")
    private String species;

    private String breed;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    private Integer age;

    // Nombre del dueño de la mascota
    private String owner;

    public Patient() {
    }

    public Patient(String name, String species, String breed, Integer age, String owner) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.owner = owner;
    }
}
