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
class EventoRecorrenciaContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldDefineRecorrenciaContract() throws Exception {
        mockMvc.perform(put("/api/v1/eventos/{eventoId}/recorrencia", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"frequencia\":\"semanal\",\"intervalo\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventoBaseId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.frequencia").value("SEMANAL"))
                .andExpect(jsonPath("$.intervalo").value(2));
    }
}
