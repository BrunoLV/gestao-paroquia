package br.com.nsfatima.calendario.domain.type;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Record representing the recurrence rules for an event.
 */
public record RegraRecorrencia(
        String frequencia,
        int intervalo,
        List<DayOfWeek> diasDaSemana,
        String posicaoNoMes,
        LocalDate dataLimite) {

    /**
     * Generates a list of dates between the specified range based on the recurrence rules.
     * 
     * Usage Example:
     * regra.gerarDatas(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
     */
    public List<LocalDate> gerarDatas(LocalDate inicio, LocalDate fim) {
        List<LocalDate> datas = new ArrayList<>();
        LocalDate atual = inicio;

        // Apply dataLimite if present
        LocalDate dataFimReal = dataLimite != null && dataLimite.isBefore(fim) ? dataLimite : fim;

        if ("SEMANAL".equalsIgnoreCase(frequencia)) {
            while (!atual.isAfter(dataFimReal)) {
                if (diasDaSemana != null && !diasDaSemana.isEmpty()) {
                    for (DayOfWeek dia : diasDaSemana) {
                        LocalDate proximo = atual.with(TemporalAdjusters.nextOrSame(dia));
                        if (!proximo.isBefore(atual) && !proximo.isAfter(dataFimReal) && !datas.contains(proximo)) {
                            datas.add(proximo);
                        }
                    }
                }
                atual = atual.plusWeeks(intervalo);
            }
        } else if ("MENSAL".equalsIgnoreCase(frequencia)) {
            while (!atual.isAfter(dataFimReal)) {
                if ("LAST".equalsIgnoreCase(posicaoNoMes) && diasDaSemana != null && !diasDaSemana.isEmpty()) {
                    for (DayOfWeek dia : diasDaSemana) {
                        LocalDate ultimoDia = atual.with(TemporalAdjusters.lastInMonth(dia));
                        if (!ultimoDia.isBefore(inicio) && !ultimoDia.isAfter(dataFimReal)) {
                            datas.add(ultimoDia);
                        }
                    }
                }
                atual = atual.plusMonths(intervalo);
            }
        }

        datas.sort(LocalDate::compareTo);
        return datas;
    }
}
