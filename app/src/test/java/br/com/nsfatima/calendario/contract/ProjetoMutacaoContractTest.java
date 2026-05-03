package br.com.nsfatima.calendario.contract;

import br.com.nsfatima.calendario.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjetoMutacaoContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
        entity.setNome("Projeto Original");
        entity.setDescricao("Descricao original");
        repository.save(entity);
    }

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
