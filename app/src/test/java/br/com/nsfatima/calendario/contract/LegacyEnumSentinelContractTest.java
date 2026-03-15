package br.com.nsfatima.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LegacyEnumSentinelContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldProjectUnknownLegacyValueOnRead() throws Exception {
        mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-0000000000aa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("UNKNOWN_LEGACY"));
    }
}
