package br.com.nsfatima.calendario.integration.eventos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should create event immediately when actor has permission")
    void shouldCreateEventImmediately() throws Exception {
        String orgId = "00000000-0000-0000-0000-000000000001";
        String payload = """
                {
                  "titulo": "Evento Teste",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-12-01T10:00:00Z",
                  "fim": "2026-12-01T12:00:00Z"
                }
                """.formatted(orgId);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Evento Teste"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Should return 400 for invalid interval")
    void shouldFailForInvalidInterval() throws Exception {
        String orgId = "00000000-0000-0000-0000-000000000001";
        String payload = """
                {
                  "titulo": "Evento Invalido",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-12-01T12:00:00Z",
                  "fim": "2026-12-01T10:00:00Z"
                }
                """.formatted(orgId);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DOMAIN_RULE_VIOLATION"));
    }

    @Test
    @DisplayName("Should return 400 for missing title")
    void shouldFailForMissingTitle() throws Exception {
        String orgId = "00000000-0000-0000-0000-000000000001";
        String payload = """
                {
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-12-01T10:00:00Z",
                  "fim": "2026-12-01T12:00:00Z"
                }
                """.formatted(orgId);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'titulo')]").exists());
    }
}
