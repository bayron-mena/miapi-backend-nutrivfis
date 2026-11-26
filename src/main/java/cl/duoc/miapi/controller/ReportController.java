package cl.duoc.miapi.controller;

import cl.duoc.miapi.dto.SalesReport;
import cl.duoc.miapi.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
// Orígenes permitidos en application.properties (app.cors.allowed-origins)
@CrossOrigin(originPatterns = "${app.cors.allowed-origins}")
@RequestMapping("/api/v1/reports")
@Tag(name = "Reportes", description = "Datos del dashboard (solo Admin)")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Resumen de ventas del período para el dashboard",
            description = "Fechas yyyy-MM-dd, ambas incluidas. Sin fechas: últimos 30 días.")
    @GetMapping("/sales")
    public SalesReport sales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.summary(from, to);
    }
}
