package br.com.nsfatima.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoParticipantesLimpezaContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldClearParticipantes() throws Exception {
        mockMvc.perform(delete("/api/v1/eventos/{eventoId}/participantes", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.organizacoesParticipantes").isArray())
                .andExpect(jsonPath("$.organizacoesParticipantes").isEmpty());
    }
}
