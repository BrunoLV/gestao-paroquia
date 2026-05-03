package br.com.nsfatima.calendario.api.v1.controller;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    void shouldAllowParocoToViewAnyEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        createEvento(eventId, UUID.randomUUID());

        mockMvc.perform(get("/api/v1/eventos/" + eventId)
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowOrgMemberToViewTheirOwnOrgEvent() throws Exception {
        UUID orgId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        createEvento(eventId, orgId);

        mockMvc.perform(get("/api/v1/eventos/" + eventId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", orgId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyOrgMemberFromViewingOtherOrgEvent() throws Exception {
        UUID otherOrgId = UUID.randomUUID();
        UUID myOrgId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        createEvento(eventId, otherOrgId);

        mockMvc.perform(get("/api/v1/eventos/" + eventId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", myOrgId.toString()))
                .andExpect(status().isForbidden());
    }

    private void createEvento(UUID id, UUID orgId) {
        EventoEntity entity = new EventoEntity();
        entity.setId(id);
        entity.setTitulo("Evento Privado");
        entity.setOrganizacaoResponsavelId(orgId);
        entity.setInicioUtc(Instant.parse("2026-05-15T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-05-15T11:00:00Z"));
        entity.setStatus("AGENDADO");
        eventoJpaRepository.save(entity);
    }
}
