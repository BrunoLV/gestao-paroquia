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
class ObservacoesContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateObservacaoWithExplicitDto() throws Exception {
        mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                        "{\"usuarioId\":\"00000000-0000-0000-0000-000000000111\",\"tipo\":\"nota\",\"conteudo\":\"Registro pastoral\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.usuarioId").value("00000000-0000-0000-0000-000000000111"))
                .andExpect(jsonPath("$.tipo").value("NOTA"))
                .andExpect(jsonPath("$.conteudo").value("Registro pastoral"));
    }

    @Test
    void shouldListObservacoesWithExplicitDto() throws Exception {
        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$[0].tipo").value("NOTA"))
                .andExpect(jsonPath("$[0].conteudo").value("Historico append-only"));
    }

    @Test
    void shouldProjectUnknownLegacyObservationType() throws Exception {
        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-0000000000aa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("UNKNOWN_LEGACY"));
    }
}
