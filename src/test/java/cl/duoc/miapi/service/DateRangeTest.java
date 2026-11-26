package cl.duoc.miapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateRangeTest {

    private static final ZoneId CHILE = ZoneId.of("America/Santiago");

    @Test
    void sinFechasSonLosUltimos30DiasIncluidoHoy() {
        DateRange range = DateRange.of(null, null, CHILE);

        assertThat(range.to()).isEqualTo(LocalDate.now(CHILE));
        assertThat(range.from()).isEqualTo(range.to().minusDays(29));
    }

    @Test
    void incluyeElDiaCompletoEnHoraDeChile() {
        LocalDate day = LocalDate.of(2026, 11, 26);
        DateRange range = DateRange.of(day, day, CHILE);

        assertThat(range.start()).isEqualTo(day.atStartOfDay(CHILE).toInstant());
        ZonedDateTime end = range.end().atZone(CHILE);
        assertThat(end.toLocalDate()).isEqualTo(day);
        assertThat(end.getHour()).isEqualTo(23);
        assertThat(end.getMinute()).isEqualTo(59);
    }

    @Test
    void desdePosteriorAHastaResponde400() {
        assertThatThrownBy(() -> DateRange.of(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1), CHILE))
                .isInstanceOf(ResponseStatusException.class);
    }
}
