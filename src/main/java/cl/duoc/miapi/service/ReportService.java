package cl.duoc.miapi.service;

import cl.duoc.miapi.dto.SalesReport;
import cl.duoc.miapi.model.Receipt;
import cl.duoc.miapi.model.ReceiptDetail;
import cl.duoc.miapi.repository.ProductRepository;
import cl.duoc.miapi.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final int TOP_PRODUCTS = 5;

    private final ReceiptRepository receiptRepository;
    private final ProductRepository productRepository;
    private final ZoneId zone;
    private final int lowStockThreshold;

    public ReportService(ReceiptRepository receiptRepository,
                         ProductRepository productRepository,
                         @Value("${app.timezone}") String timezone,
                         @Value("${app.reports.low-stock-threshold}") int lowStockThreshold) {
        this.receiptRepository = receiptRepository;
        this.productRepository = productRepository;
        this.zone = ZoneId.of(timezone);
        this.lowStockThreshold = lowStockThreshold;
    }

    // Se calcula en Java a partir de las boletas del período: es simple de leer y
    // suficiente para el volumen de una clínica.
    @Transactional(readOnly = true)
    public SalesReport summary(LocalDate from, LocalDate to) {
        DateRange range = DateRange.of(from, to, zone);
        List<Receipt> receipts =
                receiptRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(range.start(), range.end());
        List<ReceiptDetail> details = receipts.stream().flatMap(r -> r.getDetails().stream()).toList();

        long totalSales = receipts.stream().mapToLong(Receipt::getTotal).sum();
        long receiptCount = receipts.size();
        long itemsSold = details.stream().mapToLong(ReceiptDetail::getQuantity).sum();
        long customers = receipts.stream().map(Receipt::getCustomerEmail).distinct().count();

        return new SalesReport(
                range.from(), range.to(),
                totalSales,
                receiptCount,
                receiptCount == 0 ? 0 : Math.round((double) totalSales / receiptCount),
                itemsSold,
                customers,
                salesByDay(receipts, range),
                topProducts(details),
                salesByCategory(details),
                lowStockThreshold,
                productRepository.findByStockLessThanEqualOrderByStockAsc(lowStockThreshold).stream()
                        .map(p -> new SalesReport.LowStock(p.getId(), p.getName(), p.getStock()))
                        .toList());
    }

    private List<SalesReport.DailySales> salesByDay(List<Receipt> receipts, DateRange range) {
        // Parte con todos los días en 0, para que el gráfico no tenga huecos
        Map<LocalDate, long[]> byDay = new TreeMap<>();
        for (LocalDate d = range.from(); !d.isAfter(range.to()); d = d.plusDays(1)) {
            byDay.put(d, new long[2]);
        }
        for (Receipt r : receipts) {
            long[] acc = byDay.get(r.getCreatedAt().atZone(zone).toLocalDate());
            if (acc != null) {
                acc[0] += r.getTotal();
                acc[1]++;
            }
        }
        return byDay.entrySet().stream()
                .map(e -> new SalesReport.DailySales(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();
    }

    private List<SalesReport.ProductSales> topProducts(List<ReceiptDetail> details) {
        Map<String, List<ReceiptDetail>> byProduct = details.stream()
                .collect(Collectors.groupingBy(ReceiptDetail::getProductId, LinkedHashMap::new, Collectors.toList()));
        return byProduct.entrySet().stream()
                .map(e -> new SalesReport.ProductSales(
                        e.getKey(),
                        // Nombre más reciente con que se vendió
                        e.getValue().get(0).getProductName(),
                        e.getValue().stream().mapToLong(ReceiptDetail::getQuantity).sum(),
                        e.getValue().stream().mapToLong(ReceiptDetail::getSubtotal).sum()))
                .sorted(Comparator.comparingLong(SalesReport.ProductSales::quantity).reversed()
                        .thenComparing(Comparator.comparingLong(SalesReport.ProductSales::revenue).reversed()))
                .limit(TOP_PRODUCTS)
                .toList();
    }

    private List<SalesReport.CategorySales> salesByCategory(List<ReceiptDetail> details) {
        return details.stream()
                .collect(Collectors.groupingBy(ReceiptDetail::getCategoryName))
                .entrySet().stream()
                .map(e -> new SalesReport.CategorySales(
                        e.getKey(),
                        e.getValue().stream().mapToLong(ReceiptDetail::getQuantity).sum(),
                        e.getValue().stream().mapToLong(ReceiptDetail::getSubtotal).sum()))
                .sorted(Comparator.comparingLong(SalesReport.CategorySales::revenue).reversed())
                .toList();
    }
}
