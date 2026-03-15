package br.com.nsfatima.calendario.integration.projetos;

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
class VinculoProjetoEventoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldSupportProjectLinkingLifecycle() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"nome\":\"Projeto Vinculado\",\"descricao\":\"Fluxo tipado\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto Vinculado"));

        mockMvc.perform(get("/api/v1/projetos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Projeto Pastoral"));
    }
}
