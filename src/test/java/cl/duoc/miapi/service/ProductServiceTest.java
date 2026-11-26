package cl.duoc.miapi.service;

import cl.duoc.miapi.TestData;
import cl.duoc.miapi.config.FieldValidationException;
import cl.duoc.miapi.model.Category;
import cl.duoc.miapi.model.Product;
import cl.duoc.miapi.repository.CategoryRepository;
import cl.duoc.miapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    private ProductService service;
    private final Category vacunas = TestData.category(8L, "VACUNAS");

    @BeforeEach
    void setUp() {
        service = new ProductService(productRepository, categoryRepository);
        lenient().when(categoryRepository.findById(8L)).thenReturn(Optional.of(vacunas));
        lenient().when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Product nuevo(String id, int stock) {
        return TestData.product(id, "  Vacuna Nueva  ", 1500, stock, TestData.category(8L, null));
    }

    @Test
    void crearNormalizaCodigoYNombreYResuelveLaCategoria() {
        Product saved = service.create(nuevo(" zz1 ", 3));

        assertThat(saved.getId()).isEqualTo("ZZ1");
        assertThat(saved.getName()).isEqualTo("Vacuna Nueva");
        assertThat(saved.getCategory()).isSameAs(vacunas);
    }

    @Test
    void codigoRepetidoResponde409() {
        when(productRepository.existsById("ME001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(nuevo("ME001", 3)))
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void productoNuevoConStockCeroSeRechaza() {
        assertThatThrownBy(() -> service.create(nuevo("ZZ2", 0)))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(e -> assertThat(((FieldValidationException) e).getErrors()).containsKey("stock"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void categoriaInexistenteResponde400() {
        Product product = nuevo("ZZ3", 1);
        product.setCategory(TestData.category(999L, null));

        assertThatThrownBy(() -> service.create(product))
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void editarNoCambiaElCodigoYPermiteStockCero() {
        Product existente = TestData.product("ME018", "Nobivac DHPPi", 8500, 9, vacunas);
        when(productRepository.findById("ME018")).thenReturn(Optional.of(existente));
        Product datos = nuevo("OTRO", 0);

        Product updated = service.update("ME018", datos);

        assertThat(updated.getId()).isEqualTo("ME018");
        assertThat(updated.getStock()).isZero();
        assertThat(updated.getName()).isEqualTo("Vacuna Nueva");
    }

    @Test
    void updateStockSoloCambiaElStock() {
        Product existente = TestData.product("ME016", "Vetmedin 2.5mg", 28000, 6, vacunas);
        when(productRepository.findById("ME016")).thenReturn(Optional.of(existente));

        Product updated = service.updateStock("ME016", 4);

        assertThat(updated.getStock()).isEqualTo(4);
        assertThat(updated.getName()).isEqualTo("Vetmedin 2.5mg");
    }
}
