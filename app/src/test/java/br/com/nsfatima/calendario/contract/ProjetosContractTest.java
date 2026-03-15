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
class ProjetosContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldListProjetos() throws Exception {
        mockMvc.perform(get("/api/v1/projetos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isString())
                .andExpect(jsonPath("$[0].nome").value("Projeto Pastoral"))
                .andExpect(jsonPath("$[0].updated").value(false));
    }

    @Test
    void shouldCreateProjeto() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"nome\":\"Semana Santa\",\"descricao\":\"Mutirao liturgico\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Semana Santa"))
                .andExpect(jsonPath("$.descricao").value("Mutirao liturgico"))
                .andExpect(jsonPath("$.updated").value(false));
    }
}
