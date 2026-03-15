package br.com.nsfatima.calendario.contract;

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

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPatchEventoWithEnumStatus() throws Exception {
        String payload = """
                {
                  \"status\": \"confirmado\"
                }
                """;

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", "00000000-0000-0000-0000-000000000001")
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
}
