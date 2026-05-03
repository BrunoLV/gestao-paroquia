package br.com.nsfatima.calendario.integration.projetos;

import br.com.nsfatima.calendario.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjetoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should perform full project lifecycle")
    void shouldPerformFullLifecycle() throws Exception {
        // Create
        String createPayload = """
                {
                  "nome": "Projeto Lifecycle",
                  "descricao": "Teste de ciclo de vida"
                }
                """;

        String response = mockMvc.perform(post("/api/v1/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto Lifecycle"))
                .andExpect(jsonPath("$.updated").value(false))
                .andReturn().getResponse().getContentAsString();

        String projectId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        // List
        mockMvc.perform(get("/api/v1/projetos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Projeto Lifecycle')]").exists());

        // Patch
        String patchPayload = """
                {
                  "nome": "Projeto Lifecycle Atualizado"
                }
                """;

        mockMvc.perform(patch("/api/v1/projetos/{id}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto Lifecycle Atualizado"))
                .andExpect(jsonPath("$.updated").value(true));
    }

    @Test
    @DisplayName("Should fail when project name is too long")
    void shouldFailForLongName() throws Exception {
        String longName = "A".repeat(161);
        String payload = """
                {
                  "nome": "%s"
                }
                """.formatted(longName);

        mockMvc.perform(post("/api/v1/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent project")
    void shouldReturn404() throws Exception {
        String patchPayload = """
                {
                  "nome": "Inexistente"
                }
                """;

        mockMvc.perform(patch("/api/v1/projetos/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
