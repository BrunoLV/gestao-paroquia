package br.com.nsfatima.gestao.calendario.application.usecase.projeto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.calendario.api.dto.projeto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.calendario.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjetoAgregacaoServiceTest {

    @Mock
    private ProjetoEventoJpaRepository projetoRepository;

    @Mock
    private EventoJpaRepository eventoRepository;

    @Mock
    private ProjetoAgregacaoDomainService domainService;

    @InjectMocks
    private ProjetoAgregacaoService service;

    @Test
    @DisplayName("Deve retornar resumo do projeto corretamente")
    void deveRetornarResumoDoProjeto() {
        UUID projetoId = UUID.randomUUID();
        Instant agora = Instant.now();
        
        ProjetoEventoEntity projeto = new ProjetoEventoEntity();
        projeto.setId(projetoId);
        projeto.setInicioUtc(agora.minus(10, java.time.temporal.ChronoUnit.DAYS));
        projeto.setFimUtc(agora.plus(10, java.time.temporal.ChronoUnit.DAYS));

        when(projetoRepository.findById(projetoId)).thenReturn(Optional.of(projeto));
        when(eventoRepository.countByProjetoId(projetoId)).thenReturn(10L);
        when(eventoRepository.countByProjetoIdAndFimUtcLessThan(eq(projetoId), any(Instant.class))).thenReturn(6L);
        when(eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(eq(projetoId), any(Instant.class))).thenReturn(4L);
        when(eventoRepository.findInvolvedOrganizationNames(projetoId)).thenReturn(List.of("Org 1", "Org 2"));
        
        when(domainService.calcularPercentualTempoDecorrido(any(), any())).thenReturn(50.0);
        when(domainService.verificarSeEstaEmRisco(any(), any(), anyInt())).thenReturn(false);

        ProjetoResumoDTO resumo = service.obterResumo(projetoId);

        assertNotNull(resumo);
        assertEquals(10, resumo.statusExecucao().totalEventos());
        assertEquals(6, resumo.statusExecucao().eventosRealizados());
        assertEquals(4, resumo.statusExecucao().eventosPendentes());
        assertThat(resumo.mapaColaboracao().envolvidos()).containsExactlyInAnyOrder("Org 1", "Org 2");
        assertEquals(50.0, resumo.saudeTemporal().percentualTempoDecorrido());
        assertFalse(resumo.saudeTemporal().emRisco());
    }

    @Test
    @DisplayName("Deve lançar exceção quando projeto não encontrado")
    void deveLancarexcecaoQuandoProjetoNaoEncontrado() {
        UUID projetoId = UUID.randomUUID();
        when(projetoRepository.findById(projetoId)).thenReturn(Optional.empty());

        assertThrows(ProjetoNotFoundException.class, () -> service.obterResumo(projetoId));
    }
}
