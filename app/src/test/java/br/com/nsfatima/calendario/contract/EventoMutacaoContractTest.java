package br.com.nsfatima.calendario.contract;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoMutacaoContractTest {

        private static final UUID EVENTO_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private EventoJpaRepository eventoJpaRepository;

        @Test
        void shouldPatchEventoWithEnumStatus() throws Exception {
                persistEventoBase();

                String payload = """
                                {
                                  \"status\": \"confirmado\"
                                }
                                """;

                mockMvc.perform(patch("/api/v1/eventos/{eventoId}", EVENTO_ID)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "PASTORAL")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(payload))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
        }

        @Test
        void shouldRejectInvalidEnumStatusDeterministically() throws Exception {
                String payload = """
                                {
                                  \"status\": \"quinzenal\"
                                }
                                """;

                mockMvc.perform(patch("/api/v1/eventos/{eventoId}", "00000000-0000-0000-0000-000000000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ENUM_VALUE_INVALID"))
                                .andExpect(jsonPath("$.errors[0].field").value("status"));
        }

        @Test
        void shouldRejectInvalidPartialUpdateAtomically() throws Exception {
                String payload = """
                                {
                                  \"titulo\": \"Novo Titulo\",
                                  \"status\": \"inexistente\"
                                }
                                """;

                mockMvc.perform(patch("/api/v1/eventos/{eventoId}", "00000000-0000-0000-0000-000000000001")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                                .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ENUM_VALUE_INVALID"))
                                .andExpect(jsonPath("$.errors[0].field").value("status"));
        }

        @Test
        void shouldCancelEvento() throws Exception {
                mockMvc.perform(delete("/api/v1/eventos/{eventoId}", "00000000-0000-0000-0000-000000000001"))
                                .andExpect(status().isNoContent());
        }

        @SuppressWarnings("null")
        private void persistEventoBase() {
                EventoEntity entity = new EventoEntity();
                entity.setId(EVENTO_ID);
                entity.setTitulo("Evento Mutacao");
                entity.setDescricao("base");
                entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
                entity.setInicioUtc(Instant.parse("2026-03-15T10:00:00Z"));
                entity.setFimUtc(Instant.parse("2026-03-15T11:00:00Z"));
                entity.setStatus("RASCUNHO");
                eventoJpaRepository.save(entity);
        }
}
