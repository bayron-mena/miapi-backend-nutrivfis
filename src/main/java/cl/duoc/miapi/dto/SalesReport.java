package cl.duoc.miapi.dto;

import java.time.LocalDate;
import java.util.List;

// Datos del dashboard de administración para un período
public record SalesReport(
        LocalDate from,
        LocalDate to,
        // Totales del período
        long totalSales,
        long receiptCount,
        // Promedio por boleta (totalSales / receiptCount)
        long averageTicket,
        long itemsSold,
        // Compradores distintos
        long customerCount,
        // Una fila por día del período, incluidos los días sin ventas (para el gráfico)
        List<DailySales> salesByDay,
        // Los 5 productos más vendidos por cantidad
        List<ProductSales> topProducts,
        List<CategorySales> salesByCategory,
        // Productos con stock <= lowStockThreshold (para reponer)
        int lowStockThreshold,
        List<LowStock> lowStock
) {
    public record DailySales(LocalDate date, long total, long receipts) {
    }

    public record ProductSales(String productId, String productName, long quantity, long revenue) {
    }

    public record CategorySales(String categoryName, long quantity, long revenue) {
    }

    public record LowStock(String productId, String productName, int stock) {
    }
}
