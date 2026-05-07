package br.com.nsfatima.calendario.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.nsfatima.calendario.api.dto.iam.UsuarioResponse;
import br.com.nsfatima.calendario.infrastructure.security.UsuarioDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para sessão e perfil do usuário logado")
public class AuthController {

    @GetMapping("/me")
    @Operation(summary = "Obtém o perfil do usuário logado")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return new UsuarioResponse(
                userDetails.getUsuarioId(),
                userDetails.getUsername(),
                userDetails.isEnabled());
    }
}
