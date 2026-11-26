package cl.duoc.miapi.service;

import cl.duoc.miapi.TestData;
import cl.duoc.miapi.dto.ReceiptRequest;
import cl.duoc.miapi.dto.ReceiptRequest.Item;
import cl.duoc.miapi.dto.ReceiptResponse;
import cl.duoc.miapi.model.Category;
import cl.duoc.miapi.model.Product;
import cl.duoc.miapi.model.Receipt;
import cl.duoc.miapi.model.Role;
import cl.duoc.miapi.model.User;
import cl.duoc.miapi.repository.ProductRepository;
import cl.duoc.miapi.repository.ReceiptRepository;
import cl.duoc.miapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    private ReceiptService service;
    private User client;
    private Product amoxibay;
    private Product vetmedin;

    @BeforeEach
    void setUp() {
        service = new ReceiptService(receiptRepository, productRepository, userRepository);
        client = TestData.user(10L, "cliente@duoc.cl", Role.CLIENT);
        Category antibiotics = TestData.category(1L, "ANTIBIÓTICOS");
        Category cardiac = TestData.category(6L, "CARDÍACO");
        amoxibay = TestData.product("ME001", "Amoxibay 250mg", 4200, 12, antibiotics);
        vetmedin = TestData.product("ME016", "Vetmedin 2.5mg", 28000, 2, cardiac);

        lenient().when(userRepository.findByEmail("cliente@duoc.cl")).thenReturn(Optional.of(client));
        lenient().when(productRepository.findAllForUpdate(anyCollection())).thenReturn(List.of(amoxibay, vetmedin));
        lenient().when(receiptRepository.save(any())).thenAnswer(inv -> {
            Receipt receipt = inv.getArgument(0);
            receipt.setId(1L);
            return receipt;
        });
    }

    private static ReceiptRequest cart(Item... items) {
        return new ReceiptRequest(List.of(items));
    }

    private static HttpStatus statusOf(Throwable e) {
        return HttpStatus.valueOf(((ResponseStatusException) e).getStatusCode().value());
    }

    @Test
    void compraDescuentaStockYCalculaTotalConIva() {
        // ME001 aparece dos veces: se suma en una sola línea (como el carrito del frontend)
        ReceiptResponse receipt = service.create("cliente@duoc.cl", cart(
                new Item("ME001", 2, 4200),
                new Item("me016", 1, 28000),
                new Item("ME001", 1, 4200)));

        assertThat(amoxibay.getStock()).isEqualTo(9);
        assertThat(vetmedin.getStock()).isEqualTo(1);

        assertThat(receipt.number()).isEqualTo("B-000001");
        assertThat(receipt.total()).isEqualTo(4200 * 3 + 28000);
        // Neto + IVA = total, y el IVA es el 19% del neto
        assertThat(receipt.netAmount() + receipt.taxAmount()).isEqualTo(receipt.total());
        assertThat(receipt.netAmount()).isEqualTo(Math.round(40600 / 1.19));
        assertThat(receipt.itemCount()).isEqualTo(4);
        assertThat(receipt.customerEmail()).isEqualTo("cliente@duoc.cl");
        assertThat(receipt.details()).hasSize(2)
                .anySatisfy(d -> {
                    assertThat(d.productId()).isEqualTo("ME001");
                    assertThat(d.quantity()).isEqualTo(3);
                    assertThat(d.subtotal()).isEqualTo(12600);
                    assertThat(d.categoryName()).isEqualTo("ANTIBIÓTICOS");
                });
    }

    @Test
    void siUnProductoNoTieneStockNoSeDescuentaNinguno() {
        assertThatThrownBy(() -> service.create("cliente@duoc.cl", cart(
                new Item("ME001", 1, 4200),
                new Item("ME016", 5, 28000))))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.CONFLICT))
                .hasMessageContaining("Stock insuficiente para Vetmedin 2.5mg");

        assertThat(amoxibay.getStock()).isEqualTo(12);
        assertThat(vetmedin.getStock()).isEqualTo(2);
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void precioDistintoAlActualResponde409() {
        assertThatThrownBy(() -> service.create("cliente@duoc.cl", cart(new Item("ME001", 1, 1000))))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.CONFLICT))
                .hasMessageContaining("El precio cambió");
        assertThat(amoxibay.getStock()).isEqualTo(12);
    }

    @Test
    void productoInexistenteResponde409() {
        assertThatThrownBy(() -> service.create("cliente@duoc.cl", cart(new Item("NOEXISTE", 1, 1000))))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.CONFLICT))
                .hasMessageContaining("NOEXISTE");
    }

    @Test
    void cuentaBloqueadaNoPuedeComprar() {
        client.setLocked(true);

        assertThatThrownBy(() -> service.create("cliente@duoc.cl", cart(new Item("ME001", 1, 4200))))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.FORBIDDEN));
        assertThat(amoxibay.getStock()).isEqualTo(12);
    }

    @Test
    void unClienteNoPuedeVerLaBoletaDeOtro() {
        Receipt ajena = new Receipt();
        ajena.setId(5L);
        ajena.setUser(TestData.user(99L, "otro@duoc.cl", Role.CLIENT));
        when(receiptRepository.findWithDetailsById(5L)).thenReturn(Optional.of(ajena));

        // 404 y no 403: así no se revela que la boleta existe
        assertThatThrownBy(() -> service.findById(5L, "cliente@duoc.cl", false))
                .satisfies(e -> assertThat(statusOf(e)).isEqualTo(HttpStatus.NOT_FOUND));
        // Admin y Vendedor sí la ven
        assertThat(service.findById(5L, "vendedor@duoc.cl", true).id()).isEqualTo(5L);
    }
}
