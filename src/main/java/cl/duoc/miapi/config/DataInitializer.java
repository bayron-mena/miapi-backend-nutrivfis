package cl.duoc.miapi.config;

import cl.duoc.miapi.model.Category;
import cl.duoc.miapi.model.Patient;
import cl.duoc.miapi.model.Product;
import cl.duoc.miapi.model.Role;
import cl.duoc.miapi.model.User;
import cl.duoc.miapi.repository.CategoryRepository;
import cl.duoc.miapi.repository.PatientRepository;
import cl.duoc.miapi.repository.ProductRepository;
import cl.duoc.miapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    // Un usuario por rol para la demostración. Correos y claves en application.properties
    // (app.seed.*). Se crea cada uno solo si su correo no existe, así que no pisa cambios.
    @Bean
    @Order(0)
    CommandLineRunner initUsers(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.seed.users}") boolean enabled,
                                @Value("${app.seed.admin.email}") String adminEmail,
                                @Value("${app.seed.admin.password}") String adminPassword,
                                @Value("${app.seed.seller.email}") String sellerEmail,
                                @Value("${app.seed.seller.password}") String sellerPassword,
                                @Value("${app.seed.client.email}") String clientEmail,
                                @Value("${app.seed.client.password}") String clientPassword) {
        return args -> {
            if (!enabled) {
                return;
            }
            // Mismos datos que DEFAULT_ADMIN del frontend (src/data/defaultAdmin.ts)
            seedUser(userRepository, passwordEncoder, Role.ADMIN,
                    "Administrador", "Sistema", adminEmail, adminPassword,
                    LocalDate.of(2003, 5, 6), null, "Abogado 0509",
                    "Metropolitana de Santiago", "Puente Alto", "prefiero-no-decir");
            seedUser(userRepository, passwordEncoder, Role.SELLER,
                    "Vendedor", "Prueba", sellerEmail, sellerPassword,
                    LocalDate.of(1995, 3, 15), "+56911111111", "Av. Providencia 1234",
                    "Metropolitana de Santiago", "Providencia", "otro");
            seedUser(userRepository, passwordEncoder, Role.CLIENT,
                    "Cliente", "Prueba", clientEmail, clientPassword,
                    LocalDate.of(2000, 8, 20), "+56922222222", "Los Aromos 567",
                    "Valparaíso", "Viña del Mar", "prefiero-no-decir");
        };
    }

    private void seedUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          Role role, String name, String lastName, String email, String password,
                          LocalDate birthDate, String phone, String address,
                          String region, String comuna, String gender) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }
        User user = new User();
        user.setRole(role);
        user.setName(name);
        user.setLastName(lastName);
        user.setEmail(normalizedEmail);
        // Se guarda el hash BCrypt, igual que en el registro
        user.setPassword(passwordEncoder.encode(password));
        user.setBirthDate(birthDate);
        user.setPhone(phone);
        user.setAddress(address);
        user.setRegion(region);
        user.setComuna(comuna);
        user.setGender(gender);
        // termsAcceptedAt queda null: no se registraron por el formulario
        userRepository.save(user);
    }

    @Bean
    @Order(1)
    CommandLineRunner initPatients(PatientRepository patientRepository) {
        return args -> {
            if (patientRepository.count() == 0) {
                patientRepository.save(new Patient("Luna", "Perro", "Labrador", 3, "Camila Rojas"));
                patientRepository.save(new Patient("Milo", "Gato", "Siamés", 2, "Diego Muñoz"));
                patientRepository.save(new Patient("Rocky", "Perro", "Bulldog", 5, "Valentina Soto"));
            }
        };
    }

    @Bean
    @Order(2)
    CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                List<String> names = List.of(
                        "Antibióticos", "Antiparasitarios", "Antiinflamatorios",
                        "Dermatología", "Digestivo", "Cardíaco",
                        "Analgésicos", "Vacunas", "Suplementos");
                // En MAYÚSCULAS, como las guarda el frontend (normalizeCategory)
                names.forEach(name -> categoryRepository.save(new Category(name.toUpperCase())));
            }
        };
    }

    // Mismo catálogo que tenía el frontend en src/data/productos.ts
    @Bean
    @Order(3)
    CommandLineRunner initProducts(ProductRepository productRepository,
                                   CategoryRepository categoryRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }
            Map<String, Category> categories = categoryRepository.findAll().stream()
                    .collect(Collectors.toMap(c -> c.getName().toUpperCase(), Function.identity()));

            // id, nombre, categoría, precio, stock, imagen, principio activo, especie
            List<Object[]> rows = List.of(
                    new Object[]{"ME001", "Amoxibay 250mg", "Antibióticos", 4200, 12, "Amoxibay.png", "Amoxicilina — Blíster 10 comp.", "Perro / Gato"},
                    new Object[]{"ME002", "Enrox 50mg", "Antibióticos", 6800, 6, "enrrox.jpg", "Enrofloxacino — Blíster 10 comp.", "Perro / Gato"},
                    new Object[]{"ME003", "Metrobay 250mg", "Antibióticos", 3900, 18, "Metrobay.jpg", "Metronidazol — Blíster 10 comp.", "Perro / Gato"},
                    new Object[]{"ME004", "Nexgard", "Antiparasitarios", 9500, 9, "Nexgard.jpg", "Afoxolaner — Masticable 1 unid.", "Perro"},
                    new Object[]{"ME005", "Bravecto", "Antiparasitarios", 18900, 25, "Bravecto.jpg", "Fluralaner — Masticable 1 unid.", "Perro"},
                    new Object[]{"ME006", "Revolution Plus", "Antiparasitarios", 14500, 7, "RevolutionPlus.jpg", "Selamectina + Sarolaner — Pipeta 1 unid.", "Gato"},
                    new Object[]{"ME007", "Drontal Plus", "Antiparasitarios", 3200, 14, "DrontalPlus.jpg", "Praziquantel + Pamoato — Comprimido 1 unid.", "Perro"},
                    new Object[]{"ME008", "Milbemax Gato", "Antiparasitarios", 6800, 4, "MilbemaxGato.jpg", "Milbemicina + Praziq. — Comprimido 2 unid.", "Gato"},
                    new Object[]{"ME009", "Meloxicam 1mg", "Antiinflamatorios", 4500, 20, "Metrobay.jpg", "Meloxicam — Blíster 10 comp.", "Perro / Gato"},
                    new Object[]{"ME010", "Carprofen 50mg", "Antiinflamatorios", 9800, 8, "apoquel.jpg", "Carprofeno — Blíster 10 comp.", "Perro"},
                    new Object[]{"ME011", "Clorhexidina shampoo", "Dermatología", 8900, 16, "Omeprazol.jpg", "Clorhexidina 2% — Frasco 250ml", "Perro / Gato"},
                    new Object[]{"ME012", "Malaseb shampoo", "Dermatología", 12500, 10, "Tramadol.jpg", "Miconazol + Clorhex. — Frasco 250ml", "Perro / Gato"},
                    new Object[]{"ME013", "Apoquel 16mg", "Dermatología", 22000, 5, "apoquel.jpg", "Oclacitinib — Blíster 10 comp.", "Perro"},
                    new Object[]{"ME014", "Probifor", "Digestivo", 5600, 11, "probifor.jpg", "Bacillus clausii — Sobre 5ml x10", "Perro / Gato"},
                    new Object[]{"ME015", "Omeprazol 10mg vet", "Digestivo", 3800, 8, "Omeprazol.jpg", "Omeprazol — Blíster 10 comp.", "Perro / Gato"},
                    new Object[]{"ME016", "Vetmedin 2.5mg", "Cardíaco", 28000, 6, "Vetmedin.jpg", "Pimobendan — Blíster 10 comp.", "Perro"},
                    new Object[]{"ME017", "Tramadol 50mg vet", "Analgésicos", 5200, 4, "Tramadol.jpg", "Tramadol — Blíster 10 comp.", "Perro"},
                    new Object[]{"ME018", "Nobivac DHPPi", "Vacunas", 8500, 9, "NobivacDHPPi.jpg", "Vacuna polivalente — Vial 1 dosis", "Perro"},
                    new Object[]{"ME019", "Nobivac Rabies", "Vacunas", 5800, 12, "NobivacRabies.jpg", "Vacuna antirrábica — Vial 1 dosis", "Perro / Gato"},
                    new Object[]{"ME020", "Felocell CVR", "Vacunas", 7200, 15, "FelocellCVR.jpg", "Vacuna triple felina — Vial 1 dosis", "Gato"},
                    new Object[]{"ME021", "Omega vet 3-6-9", "Suplementos", 9900, 10, "Omegavet 3-6-9.jpg", "Ácidos grasos omega — Frasco 100ml", "Perro / Gato"},
                    new Object[]{"ME022", "Condrovet forte", "Suplementos", 14500, 13, "Condrovetforte.jpg", "Condroitín + Glucos. — Blíster 30 comp.", "Perro"}
            );

            for (Object[] r : rows) {
                productRepository.save(new Product(
                        (String) r[0], (String) r[1],
                        // Misma distribuidora por defecto que usa el frontend (AppProvider.tsx)
                        "Distribuidora VetSur",
                        (Integer) r[3], (Integer) r[4],
                        (String) r[5], (String) r[6], (String) r[7],
                        categories.get(((String) r[2]).toUpperCase())));
            }
        };
    }
}
