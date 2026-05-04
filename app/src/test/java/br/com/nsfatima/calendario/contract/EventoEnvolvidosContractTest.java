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
class EventoEnvolvidosContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldUpdateEnvolvidos() throws Exception {
        mockMvc.perform(put("/api/v1/eventos/{eventoId}/envolvidos", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"envolvidos\":[{\"organizacaoId\":\"00000000-0000-0000-0000-000000000111\", \"papel\": \"ORGANIZADOR\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.envolvidos[0].organizacaoId").value("00000000-0000-0000-0000-000000000111"))
                .andExpect(jsonPath("$.envolvidos[0].papel").value("ORGANIZADOR"));
    }
}
