package cl.duoc.miapi.service;

import cl.duoc.miapi.TestData;
import cl.duoc.miapi.dto.SalesReport;
import cl.duoc.miapi.model.Receipt;
import cl.duoc.miapi.model.ReceiptDetail;
import cl.duoc.miapi.repository.ProductRepository;
import cl.duoc.miapi.repository.ReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final ZoneId CHILE = ZoneId.of("America/Santiago");

    @Mock
    private ReceiptRepository receiptRepository;
    @Mock
    private ProductRepository productRepository;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(receiptRepository, productRepository, "America/Santiago", 5);
    }

    private static Receipt receipt(String email, LocalDateTime when, ReceiptDetail... details) {
        Receipt r = new Receipt();
        r.setCustomerEmail(email);
        r.setCreatedAt(when.atZone(CHILE).toInstant());
        for (ReceiptDetail d : details) {
            r.addDetail(d);
        }
        r.setTotal(r.getDetails().stream().mapToInt(ReceiptDetail::getSubtotal).sum());
        return r;
    }

    private static ReceiptDetail line(String id, String name, String category, int price, int qty) {
        ReceiptDetail d = new ReceiptDetail();
        d.setProductId(id);
        d.setProductName(name);
        d.setCategoryName(category);
        d.setUnitPrice(price);
        d.setQuantity(qty);
        d.setSubtotal(price * qty);
        return d;
    }

    @Test
    void resumenSumaVentasPorDiaProductoYCategoria() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 3);
        // 23:30 del día 1 en Chile: debe contar para el día 1 aunque en UTC ya sea el día 2
        when(receiptRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(any(), any())).thenReturn(List.of(
                receipt("a@duoc.cl", LocalDateTime.of(2026, 11, 1, 23, 30),
                        line("ME001", "Amoxibay", "ANTIBIÓTICOS", 4200, 3)),
                receipt("b@duoc.cl", LocalDateTime.of(2026, 11, 3, 10, 0),
                        line("ME016", "Vetmedin", "CARDÍACO", 28000, 1),
                        line("ME001", "Amoxibay", "ANTIBIÓTICOS", 4200, 1)),
                receipt("a@duoc.cl", LocalDateTime.of(2026, 11, 3, 12, 0),
                        line("ME002", "Enrox", "ANTIBIÓTICOS", 6800, 2))));
        when(productRepository.findByStockLessThanEqualOrderByStockAsc(5))
                .thenReturn(List.of(TestData.product("ME008", "Milbemax Gato", 6800, 4, null)));

        SalesReport report = service.summary(from, to);

        assertThat(report.totalSales()).isEqualTo(12600 + 32200 + 13600);
        assertThat(report.receiptCount()).isEqualTo(3);
        assertThat(report.averageTicket()).isEqualTo(Math.round(58400 / 3.0));
        assertThat(report.itemsSold()).isEqualTo(7);
        assertThat(report.customerCount()).isEqualTo(2);

        // Un registro por día, incluido el día 2 sin ventas
        assertThat(report.salesByDay()).extracting(SalesReport.DailySales::date)
                .containsExactly(from, from.plusDays(1), to);
        assertThat(report.salesByDay().get(0).total()).isEqualTo(12600);
        assertThat(report.salesByDay().get(1).total()).isZero();
        assertThat(report.salesByDay().get(2).receipts()).isEqualTo(2);

        // El más vendido por cantidad va primero
        assertThat(report.topProducts().get(0).productId()).isEqualTo("ME001");
        assertThat(report.topProducts().get(0).quantity()).isEqualTo(4);
        // Ordenadas por monto: ANTIBIÓTICOS 12.600 + 4.200 + 13.600 = 30.400 > CARDÍACO 28.000
        assertThat(report.salesByCategory()).extracting(SalesReport.CategorySales::categoryName)
                .containsExactly("ANTIBIÓTICOS", "CARDÍACO");
        assertThat(report.salesByCategory().get(0).revenue()).isEqualTo(30400);
        assertThat(report.lowStock()).extracting(SalesReport.LowStock::productId).containsExactly("ME008");
    }

    @Test
    void periodoSinVentasDevuelveCeros() {
        when(receiptRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(any(), any())).thenReturn(List.of());

        SalesReport report = service.summary(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 1));

        assertThat(report.totalSales()).isZero();
        assertThat(report.averageTicket()).isZero();
        assertThat(report.topProducts()).isEmpty();
        assertThat(report.salesByDay()).hasSize(1);
    }
}
