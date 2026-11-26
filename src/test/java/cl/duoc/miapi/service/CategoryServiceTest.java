package cl.duoc.miapi.service;

import cl.duoc.miapi.TestData;
import cl.duoc.miapi.model.Category;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categoryRepository, productRepository);
    }

    @Test
    void crearGuardaEnMayusculasSinEspaciosSobrantes() {
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Category saved = service.create(new Category("  nutrición felina "));

        assertThat(saved.getName()).isEqualTo("NUTRICIÓN FELINA");
    }

    @Test
    void nombreRepetidoSinImportarMayusculasResponde409() {
        when(categoryRepository.existsByNameIgnoreCase("VACUNAS")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new Category("Vacunas")))
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void noSeEliminaUnaCategoriaConProductos() {
        when(categoryRepository.findById(8L)).thenReturn(Optional.of(TestData.category(8L, "VACUNAS")));
        when(productRepository.existsByCategoryId(8L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(8L))
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
        verify(categoryRepository, never()).delete(any());
    }
}
