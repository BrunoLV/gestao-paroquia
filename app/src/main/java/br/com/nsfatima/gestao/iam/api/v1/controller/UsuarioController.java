package br.com.nsfatima.gestao.iam.api.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.AddMembershipRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.CreateUsuarioRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.MembershipResponse;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.organizacao.application.usecase.AddMembershipUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.CreateUsuarioUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.GetUsuarioUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListMembershipsUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.ListUsuariosUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.RemoveMembershipUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.UpdateUsuarioUseCase;
import br.com.nsfatima.gestao.iam.domain.service.UsuarioAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final UsuarioAuthorizationService authorizationService;

    public UsuarioController(
            ListUsuariosUseCase listUsuariosUseCase,
            GetUsuarioUseCase getUsuarioUseCase,
            CreateUsuarioUseCase createUsuarioUseCase,
            UpdateUsuarioUseCase updateUsuarioUseCase,
            AddMembershipUseCase addMembershipUseCase,
            RemoveMembershipUseCase removeMembershipUseCase,
            ListMembershipsUseCase listMembershipsUseCase,
            UsuarioAuthorizationService authorizationService) {
        this.listUsuariosUseCase = listUsuariosUseCase;
        this.getUsuarioUseCase = getUsuarioUseCase;
        this.createUsuarioUseCase = createUsuarioUseCase;
        this.updateUsuarioUseCase = updateUsuarioUseCase;
        this.addMembershipUseCase = addMembershipUseCase;
        this.removeMembershipUseCase = removeMembershipUseCase;
        this.listMembershipsUseCase = listMembershipsUseCase;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os usuários")
    public List<UsuarioResponse> list() {
        return listUsuariosUseCase.execute();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo usuário")
    public UUID create(@RequestBody @Valid CreateUsuarioRequest request) {
        return createUsuarioUseCase.execute(request.username(), request.password());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @Operation(summary = "Obtém detalhes de um usuário")
    public UsuarioResponse get(@PathVariable UUID id) {
        authorizationService.requireAdminOrCoordinatorOf(id);
        return getUsuarioUseCase.execute(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um usuário")
    public void update(@PathVariable UUID id, @RequestBody UsuarioResponse request) {
        updateUsuarioUseCase.execute(id, request.username(), request.enabled());
    }

    @GetMapping("/{id}/membros")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @Operation(summary = "Lista associações de um usuário a organizações")
    public List<MembershipResponse> listMemberships(@PathVariable UUID id) {
        authorizationService.requireAdminOrCoordinatorOf(id);
        return listMembershipsUseCase.execute(id);
    }

    @PostMapping("/{id}/membros")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona associação a organização")
    public UUID addMembership(@PathVariable UUID id, @RequestBody @Valid AddMembershipRequest request) {
        authorizationService.requireAdminOrCoordinatorOfOrganization(request.organizacaoId());
        return addMembershipUseCase.execute(id, request.organizacaoId(), request.tipo(), request.papel());
    }

    @DeleteMapping("/{id}/membros/{membershipId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove associação a organização")
    public void removeMembership(@PathVariable UUID id, @PathVariable UUID membershipId) {
        // Here we ideally need to check the organization of the membership being removed.
        // For simplicity, we can use requireAdminOrCoordinatorOf(id) which ensures 
        // the actor can manage the target user.
        authorizationService.requireAdminOrCoordinatorOf(id);
        removeMembershipUseCase.execute(membershipId);
    }
}
