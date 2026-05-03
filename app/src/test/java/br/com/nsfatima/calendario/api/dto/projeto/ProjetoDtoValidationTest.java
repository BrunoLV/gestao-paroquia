package br.com.nsfatima.calendario.api.dto.projeto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjetoDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("ProjetoCreateRequest: should fail when name is too long")
    void projetoCreateRequest_NameTooLong() {
        String longName = "A".repeat(161);
        ProjetoCreateRequest request = new ProjetoCreateRequest(longName, "Descricao");

        Set<ConstraintViolation<ProjetoCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long name");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }

    @Test
    @DisplayName("ProjetoCreateRequest: should fail when description is too long")
    void projetoCreateRequest_DescriptionTooLong() {
        String longDescription = "A".repeat(2001);
        ProjetoCreateRequest request = new ProjetoCreateRequest("Nome", longDescription);

        Set<ConstraintViolation<ProjetoCreateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long description");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("descricao")));
    }

    @Test
    @DisplayName("ProjetoPatchRequest: should fail when name is too long")
    void projetoPatchRequest_NameTooLong() {
        String longName = "A".repeat(161);
        ProjetoPatchRequest request = new ProjetoPatchRequest(longName, "Descricao");

        Set<ConstraintViolation<ProjetoPatchRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for long name");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }

    @Test
    @DisplayName("ProjetoPatchRequest: should fail when name is blank")
    void projetoPatchRequest_NameBlank() {
        ProjetoPatchRequest request = new ProjetoPatchRequest(" ", "Descricao");

        Set<ConstraintViolation<ProjetoPatchRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should have violations for blank name");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }
}
