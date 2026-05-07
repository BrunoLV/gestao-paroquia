package br.com.nsfatima.calendario.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.iam.AddMembershipRequest;
import br.com.nsfatima.calendario.api.dto.iam.CreateUsuarioRequest;
import br.com.nsfatima.calendario.api.dto.iam.MembershipResponse;
import br.com.nsfatima.calendario.api.dto.iam.UsuarioResponse;
import br.com.nsfatima.calendario.application.usecase.iam.AddMembershipUseCase;
import br.com.nsfatima.calendario.application.usecase.iam.CreateUsuarioUseCase;
import br.com.nsfatima.calendario.application.usecase.iam.GetUsuarioUseCase;
import br.com.nsfatima.calendario.application.usecase.iam.ListMembershipsUseCase;
import br.com.nsfatima.calendario.application.usecase.iam.ListUsuariosUseCase;
import br.com.nsfatima.calendario.application.usecase.iam.RemoveMembershipUseCase;
import br.com.nsfatima.calendario.application.usecase.iam.UpdateUsuarioUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários e permissões (IAM)")
public class UsuarioController {

    private final ListUsuariosUseCase listUsuariosUseCase;
    private final GetUsuarioUseCase getUsuarioUseCase;
    private final CreateUsuarioUseCase createUsuarioUseCase;
    private final UpdateUsuarioUseCase updateUsuarioUseCase;
    private final AddMembershipUseCase addMembershipUseCase;
    private final RemoveMembershipUseCase removeMembershipUseCase;
    private final ListMembershipsUseCase listMembershipsUseCase;

    public UsuarioController(
            ListUsuariosUseCase listUsuariosUseCase,
            GetUsuarioUseCase getUsuarioUseCase,
            CreateUsuarioUseCase createUsuarioUseCase,
            UpdateUsuarioUseCase updateUsuarioUseCase,
            AddMembershipUseCase addMembershipUseCase,
            RemoveMembershipUseCase removeMembershipUseCase,
            ListMembershipsUseCase listMembershipsUseCase) {
        this.listUsuariosUseCase = listUsuariosUseCase;
        this.getUsuarioUseCase = getUsuarioUseCase;
        this.createUsuarioUseCase = createUsuarioUseCase;
        this.updateUsuarioUseCase = updateUsuarioUseCase;
        this.addMembershipUseCase = addMembershipUseCase;
        this.removeMembershipUseCase = removeMembershipUseCase;
        this.listMembershipsUseCase = listMembershipsUseCase;
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários")
    public List<UsuarioResponse> list() {
        return listUsuariosUseCase.execute();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo usuário")
    public UUID create(@RequestBody @Valid CreateUsuarioRequest request) {
        return createUsuarioUseCase.execute(request.username(), request.password());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém detalhes de um usuário")
    public UsuarioResponse get(@PathVariable UUID id) {
        return getUsuarioUseCase.execute(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualiza um usuário")
    public void update(@PathVariable UUID id, @RequestBody UsuarioResponse request) {
        updateUsuarioUseCase.execute(id, request.username(), request.enabled());
    }

    @GetMapping("/{id}/membros")
    @Operation(summary = "Lista associações de um usuário a organizações")
    public List<MembershipResponse> listMemberships(@PathVariable UUID id) {
        return listMembershipsUseCase.execute(id);
    }

    @PostMapping("/{id}/membros")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona associação a organização")
    public UUID addMembership(@PathVariable UUID id, @RequestBody @Valid AddMembershipRequest request) {
        return addMembershipUseCase.execute(id, request.organizacaoId(), request.tipo(), request.papel());
    }

    @DeleteMapping("/{id}/membros/{membershipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove associação a organização")
    public void removeMembership(@PathVariable UUID id, @PathVariable UUID membershipId) {
        removeMembershipUseCase.execute(membershipId);
    }
}
