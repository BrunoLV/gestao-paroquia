package br.com.nsfatima.gestao.calendario.application.usecase.projeto;

import br.com.nsfatima.gestao.calendario.api.dto.projeto.MapaColaboracaoDTO;
import br.com.nsfatima.gestao.calendario.api.dto.projeto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.calendario.api.dto.projeto.SaudeTemporalDTO;
import br.com.nsfatima.gestao.calendario.api.dto.projeto.StatusExecucaoDTO;
import br.com.nsfatima.gestao.calendario.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.service.ProjetoAgregacaoDomainService;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de aplicação para agregação de dados de projetos.
 */
@Service
public class ProjetoAgregacaoService {

    private final ProjetoEventoJpaRepository projetoRepository;
    private final EventoJpaRepository eventoRepository;
    private final ProjetoAgregacaoDomainService domainService;

    public ProjetoAgregacaoService(
            ProjetoEventoJpaRepository projetoRepository,
            EventoJpaRepository eventoRepository,
            ProjetoAgregacaoDomainService domainService) {
        this.projetoRepository = projetoRepository;
        this.eventoRepository = eventoRepository;
        this.domainService = domainService;
    }

    /**
     * Obtém o resumo agregado de um projeto.
     *
     * @param projetoId ID do projeto
     * @return DTO com o resumo agregado
     * @throws ProjetoNotFoundException se o projeto não for encontrado
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.PROJECT_RESUMO_CACHE, key = "#projetoId")
    public ProjetoResumoDTO obterResumo(UUID projetoId) {
        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ProjetoNotFoundException(projetoId));

        Instant agora = Instant.now();

        // Status de Execução
        int totalEventos = (int) eventoRepository.countByProjetoId(projetoId);
        int eventosRealizados = (int) eventoRepository.countByProjetoIdAndFimUtcLessThan(projetoId, agora);
        int eventosPendentes = (int) eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(projetoId, agora);

        StatusExecucaoDTO statusExecucao = new StatusExecucaoDTO(totalEventos, eventosRealizados, eventosPendentes);

        // Mapa de Colaboração
        List<String> envolvidos = eventoRepository.findInvolvedOrganizationNames(projetoId);
        MapaColaboracaoDTO mapaColaboracao = new MapaColaboracaoDTO(envolvidos);

        // Saúde Temporal
        double percentual = domainService.calcularPercentualTempoDecorrido(projeto.getInicioUtc(), projeto.getFimUtc());
        boolean emRisco = domainService.verificarSeEstaEmRisco(projeto.getInicioUtc(), projeto.getFimUtc(), eventosPendentes);
        SaudeTemporalDTO saudeTemporal = new SaudeTemporalDTO(percentual, emRisco);

        return new ProjetoResumoDTO(statusExecucao, mapaColaboracao, saudeTemporal);
    }
}
