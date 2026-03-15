package br.com.nsfatima.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AprovacoesContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAprovacaoWithExplicitDto() throws Exception {
        mockMvc.perform(post("/api/v1/aprovacoes")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                        "{\"eventoId\":\"00000000-0000-0000-0000-000000000001\",\"tipoSolicitacao\":\"alteracao_horario\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.tipoSolicitacao").value("ALTERACAO_HORARIO"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }
}
