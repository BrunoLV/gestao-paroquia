package br.com.nsfatima.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjetoMutacaoContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPatchProjeto() throws Exception {
        mockMvc.perform(patch("/api/v1/projetos/{projetoId}", "00000000-0000-0000-0000-000000000010")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"nome\":\"Projeto Atualizado\",\"descricao\":\"Planejamento revisado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000010"))
                .andExpect(jsonPath("$.nome").value("Projeto Atualizado"))
                .andExpect(jsonPath("$.descricao").value("Planejamento revisado"))
                .andExpect(jsonPath("$.updated").value(true));
    }
}
