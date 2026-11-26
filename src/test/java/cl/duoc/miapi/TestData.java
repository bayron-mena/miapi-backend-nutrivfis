package cl.duoc.miapi;

import cl.duoc.miapi.dto.RegisterRequest;
import cl.duoc.miapi.model.Category;
import cl.duoc.miapi.model.Product;
import cl.duoc.miapi.model.Role;
import cl.duoc.miapi.model.User;

import java.time.LocalDate;

// Datos de prueba reutilizables para los tests unitarios
public final class TestData {

    private TestData() {
    }

    public static RegisterRequest validRegister() {
        return new RegisterRequest(
                "Ana", "Pérez", "Ana123@Gmail.com", "Clave1",
                LocalDate.of(2000, 5, 6), "+", "Av. Siempre Viva 123",
                "Metropolitana de Santiago", "Puente Alto", "femenino", true);
    }

    public static User user(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("Usuario");
        user.setLastName("Prueba");
        user.setEmail(email);
        user.setPassword("hash");
        user.setRole(role);
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setAddress("Calle 1");
        user.setRegion("Maule");
        user.setComuna("Talca");
        user.setGender("otro");
        return user;
    }

    public static Category category(Long id, String name) {
        Category category = new Category(name);
        category.setId(id);
        return category;
    }

    public static Product product(String id, String name, int price, int stock, Category category) {
        return new Product(id, name, "Distribuidora VetSur", price, stock,
                "img.jpg", "Principio", "Perro", category);
    }
}
