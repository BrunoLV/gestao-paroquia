package br.com.nsfatima.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoParticipantesContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUpdateParticipantes() throws Exception {
        mockMvc.perform(put("/api/v1/eventos/{eventoId}/participantes", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"organizacoesParticipantes\":[\"00000000-0000-0000-0000-000000000111\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.organizacoesParticipantes[0]").value("00000000-0000-0000-0000-000000000111"));
    }
}
