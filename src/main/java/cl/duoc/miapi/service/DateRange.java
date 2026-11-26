package cl.duoc.miapi.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

// Período de fechas (ambos días incluidos) en la hora de Chile.
// Ej.: from=2026-11-01, to=2026-11-30 -> desde el 1 a las 00:00 hasta el 30 a las 23:59:59.
public record DateRange(LocalDate from, LocalDate to, Instant start, Instant end) {

    // Si no se indican fechas: los últimos 30 días, incluido hoy
    public static final int DEFAULT_DAYS = 30;

    public static DateRange of(LocalDate from, LocalDate to, ZoneId zone) {
        LocalDate end = to != null ? to : LocalDate.now(zone);
        LocalDate start = from != null ? from : end.minusDays(DEFAULT_DAYS - 1);
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'.");
        }
        return new DateRange(start, end,
                start.atStartOfDay(zone).toInstant(),
                end.plusDays(1).atStartOfDay(zone).toInstant().minusNanos(1));
    }
}
