package br.com.nsfatima.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventosContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListEventosPublicly() throws Exception {
        mockMvc.perform(get("/api/v1/eventos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMADO"));
    }

    @Test
    void shouldCreateEventoWithCanonicalEnumNormalization() throws Exception {
        String payload = """
                      {
                        \"titulo\": \"Missa\",
                        \"inicio\": \"2026-03-15T10:00:00Z\",
                \"fim\": \"2026-03-15T11:00:00Z\",
                \"status\": \"  adicionado_extra  \",
                \"adicionadoExtraJustificativa\": \"Cobertura extraordinaria\"
                      }
                      """;

        mockMvc.perform(post("/api/v1/eventos")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ADICIONADO_EXTRA"));
    }
}
