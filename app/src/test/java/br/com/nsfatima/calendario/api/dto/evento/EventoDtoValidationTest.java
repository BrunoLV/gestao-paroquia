package br.com.nsfatima.calendario.api.dto.evento;

import br.com.nsfatima.calendario.domain.type.EventoStatusInput;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("CreateEventoRequest: should fail when title is too long")
    void createEventoRequest_TitleTooLong() {
        String longTitle = "A".repeat(161);
        CreateEventoRequest request = new CreateEventoRequest(
                longTitle,
                "Descricao",
                UUID.randomUUID(),
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null
        );

        Set<ConstraintViolation<CreateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    @DisplayName("CreateEventoRequest: should fail when description is too long")
    void createEventoRequest_DescriptionTooLong() {
        String longDescription = "A".repeat(4001);
        CreateEventoRequest request = new CreateEventoRequest(
                "Titulo",
                longDescription,
                UUID.randomUUID(),
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null
        );

        Set<ConstraintViolation<CreateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long description");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricao")));
    }

    @Test
    @DisplayName("UpdateEventoRequest: should fail when title is blank")
    void updateEventoRequest_TitleBlank() {
        UpdateEventoRequest request = new UpdateEventoRequest(
                " ",
                "Descricao",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null,
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<UpdateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for blank title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    @DisplayName("UpdateEventoRequest: should fail when title is too long")
    void updateEventoRequest_TitleTooLong() {
        String longTitle = "A".repeat(161);
        UpdateEventoRequest request = new UpdateEventoRequest(
                longTitle,
                "Descricao",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                EventoStatusInput.RASCUNHO,
                null,
                null,
                UUID.randomUUID(),
                null
        );

        Set<ConstraintViolation<UpdateEventoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long title");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("titulo")));
    }
}
