package br.com.nsfatima.calendario.domain.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegraRecorrenciaTest {

    @Test
    @DisplayName("Should generate dates for a weekly recurrence (Sundays)")
    void shouldGenerateWeeklyDates() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "SEMANAL",
                1,
                List.of(DayOfWeek.SUNDAY),
                null,
                null);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // 2026-01-04 (Sun), 11, 18, 25
        assertEquals(4, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 1, 4)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 11)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 18)));
        assertTrue(dates.contains(LocalDate.of(2026, 1, 25)));
    }

    @Test
    @DisplayName("Should generate dates for a monthly recurrence (Last Friday of the month)")
    void shouldGenerateMonthlyLastFridayDates() {
        RegraRecorrencia regra = new RegraRecorrencia(
                "MENSAL",
                1,
                List.of(DayOfWeek.FRIDAY),
                "LAST",
                null);

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 3, 31);

        List<LocalDate> dates = regra.gerarDatas(start, end);

        // Jan 2026 last Friday: Jan 30
        // Feb 2026 last Friday: Feb 27
        // Mar 2026 last Friday: Mar 27
        assertEquals(3, dates.size());
        assertTrue(dates.contains(LocalDate.of(2026, 1, 30)));
        assertTrue(dates.contains(LocalDate.of(2026, 2, 27)));
        assertTrue(dates.contains(LocalDate.of(2026, 3, 27)));
    }
}
