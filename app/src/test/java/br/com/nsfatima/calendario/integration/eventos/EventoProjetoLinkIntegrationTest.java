package br.com.nsfatima.calendario.integration.eventos;

import br.com.nsfatima.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoProjetoLinkIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository projetoRepository;

    @Autowired
    private EventoJpaRepository eventoRepository;

    private UUID projetoId;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        eventoRepository.deleteAll();
        projetoRepository.deleteAll();
        ProjetoEventoEntity projeto = new ProjetoEventoEntity();
        projeto.setId(UUID.randomUUID());
        projeto.setNome("Projeto Teste");
        projeto.setOrganizacaoResponsavelId(orgId);
        projeto.setInicioUtc(Instant.parse("2026-01-01T00:00:00Z"));
        projeto.setFimUtc(Instant.parse("2026-01-31T23:59:59Z"));
        projeto.setStatus("ATIVO");
        projetoId = projetoRepository.save(projeto).getId();
    }

    @Test
    void shouldLinkEventToProjectWithinTimeframe() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento do Projeto",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-01-10T10:00:00Z",
                          "fim": "2026-01-10T12:00:00Z"
                        }
                        """.formatted(orgId, projetoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projetoId").value(projetoId.toString()))
                .andExpect(jsonPath("$.nomeProjeto").value("Projeto Teste"));
    }

    @Test
    void shouldFailWhenEventIsOutsideProjectTimeframe() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento Fora do Prazo",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-02-01T10:00:00Z",
                          "fim": "2026-02-01T12:00:00Z"
                        }
                        """.formatted(orgId, projetoId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenProjectIsInactive() throws Exception {
        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId).get();
        projeto.setStatus("INATIVO");
        projetoRepository.save(projeto);

        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento em Projeto Inativo",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-01-10T10:00:00Z",
                          "fim": "2026-01-10T12:00:00Z"
                        }
                        """.formatted(orgId, projetoId)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldFilterEventsByProject() throws Exception {
        // 1. Create event linked to project
        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento do Projeto",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-01-10T10:00:00Z",
                          "fim": "2026-01-10T12:00:00Z"
                        }
                        """.formatted(orgId, projetoId)))
                .andExpect(status().isCreated());

        // 2. List with project filter
        mockMvc.perform(get("/api/v1/eventos")
                .param("projeto_id", projetoId.toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Evento do Projeto"))
                .andExpect(jsonPath("$.content[0].nomeProjeto").value("Projeto Teste"));
    }
}
