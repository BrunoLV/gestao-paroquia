package br.com.nsfatima.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import br.com.nsfatima.calendario.support.SecurityTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class CalendarPayloadCompatibilityContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPreserveApprovalPayloadShapeForAuthenticatedUsers() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "ana.conselho", "senha123");

        mockMvc.perform(post("/api/v1/aprovacoes")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"eventoId\": \"00000000-0000-0000-0000-000000000001\",
                                  \"tipoSolicitacao\": \"alteracao_horario\"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.tipoSolicitacao").value("ALTERACAO_HORARIO"))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void shouldPreserveParticipantesCleanupPayloadForAuthenticatedUsers() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}/participantes", "00000000-0000-0000-0000-000000000001")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventoId").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.organizacoesParticipantes").isArray());
    }
}
