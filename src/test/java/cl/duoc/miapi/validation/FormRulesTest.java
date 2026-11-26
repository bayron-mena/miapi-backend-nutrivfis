package cl.duoc.miapi.validation;

import cl.duoc.miapi.TestData;
import cl.duoc.miapi.dto.RegisterRequest;
import cl.duoc.miapi.model.Category;
import cl.duoc.miapi.model.Product;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

// Las reglas de @Valid deben coincidir con las del frontend
// (src/utils/validaciones.ts, registro.ts y administracion.ts).
class FormRulesTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static <T> Set<String> invalidFields(T object) {
        return validator.validate(object).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    // ---- Registro ----

    @Test
    void registroValidoNoTieneErrores() {
        assertThat(invalidFields(TestData.validRegister())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "abcdefgh", "Abcdefghijklmn"}) // muy corta, sin mayúscula, 14 caracteres
    void claveFueraDeReglaSeRechaza(String password) {
        RegisterRequest request = TestData.validRegister();
        request.setPassword(password);
        assertThat(invalidFields(request)).contains("password");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ana.perez@gmail.com", "ana@hotmail.com", "a234567890123456789012345@duoc.cl"})
    void correoFueraDeReglaSeRechaza(String email) {
        RegisterRequest request = TestData.validRegister();
        request.setEmail(email);
        assertThat(invalidFields(request)).contains("email");
    }

    @Test
    void telefonoOpcionalPeroConFormato() {
        RegisterRequest request = TestData.validRegister();
        request.setPhone("+56912345678");
        assertThat(invalidFields(request)).isEmpty();
        request.setPhone("+5691234");
        assertThat(invalidFields(request)).contains("phone");
    }

    @Test
    void sinAceptarCondicionesSeRechaza() {
        RegisterRequest request = TestData.validRegister();
        request.setAcceptTerms(false);
        assertThat(invalidFields(request)).contains("acceptTerms");
        request.setAcceptTerms(null);
        assertThat(invalidFields(request)).contains("acceptTerms");
    }

    // ---- Productos ----

    private static Product productNamed(String name) {
        return TestData.product("ZZ1", name, 1500, 1, TestData.category(1L, "VACUNAS"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Vetmedin 2.5mg", "Omega vet 3-6-9", "Amoxicilina + Ác. clavulánico (500mg)",
            "Clorhexidina 2%", "Lab. O'Brien & Cía"})
    void nombresConSimbolosComunesSeAceptan(String name) {
        assertThat(invalidFields(productNamed(name))).doesNotContain("name");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "A!B", "Vet<script>", "   "})
    void nombresInvalidosSeRechazan(String name) {
        assertThat(invalidFields(productNamed(name))).contains("name");
    }

    @Test
    void precioMinimoEs100() {
        Product product = productNamed("Vacuna Nueva");
        product.setPrice(99);
        assertThat(invalidFields(product)).contains("price");
        product.setPrice(100);
        assertThat(invalidFields(product)).doesNotContain("price");
    }

    // ---- Categorías ----

    @ParameterizedTest
    @ValueSource(strings = {"ab", "abc1", "abc-"})
    void categoriaSoloLetrasYMinimoTres(String name) {
        assertThat(invalidFields(new Category(name))).contains("name");
    }

    @Test
    void categoriaConAcentosYEspaciosEsValida() {
        assertThat(invalidFields(new Category("nutrición felina"))).isEmpty();
    }
}
