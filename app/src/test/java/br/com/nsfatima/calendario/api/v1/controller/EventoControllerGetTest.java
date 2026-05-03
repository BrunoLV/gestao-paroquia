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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoControllerGetTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    void shouldReturnEventDetailsWhenFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventoEntity entity = new EventoEntity();
        entity.setId(eventId);
        entity.setTitulo("Evento Teste Get");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        entity.setInicioUtc(Instant.parse("2026-05-15T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-05-15T11:00:00Z"));
        entity.setStatus("AGENDADO");
        eventoJpaRepository.save(entity);

        mockMvc.perform(get("/api/v1/eventos/" + eventId)
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.titulo").value("Evento Teste Get"));
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/eventos/" + UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isNotFound());
    }
}
