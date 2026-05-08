package br.com.nsfatima.gestao.organizacao.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizationBusinessException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganizacaoServiceTest {

    private OrganizacaoService service;
    private OrganizacaoRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(OrganizacaoRepository.class);
        service = new OrganizacaoService(repository);
    }

    @Test
    void shouldCreateOrganization() {
        Organizacao created = service.createOrganization("PJ", TipoOrganizacao.MOVIMENTO, "pj@test.com");
        
        assertNotNull(created.getId());
        assertEquals("PJ", created.getNome());
        assertTrue(created.isAtivo());
        verify(repository).save(any(Organizacao.class));
    }

    @Test
    void shouldUpdateOrganization() {
        UUID id = UUID.randomUUID();
        Organizacao org = new Organizacao(id, "PJ", TipoOrganizacao.MOVIMENTO, "pj@test.com", true);
        when(repository.findById(id)).thenReturn(Optional.of(org));

        service.updateOrganization(id, "PJ Novo", TipoOrganizacao.PASTORAL, "pj2@test.com", false);

        assertEquals("PJ Novo", org.getNome());
        assertEquals(TipoOrganizacao.PASTORAL, org.getTipo());
        assertFalse(org.isAtivo());
        verify(repository).save(org);
    }

    @Test
    void shouldThrowExceptionWhenDeletingOrgInUse() {
        UUID id = UUID.randomUUID();
        when(repository.hasDependencies(id)).thenReturn(true);

        assertThrows(OrganizationBusinessException.class, () -> service.deleteOrganization(id));
        verify(repository, never()).delete(id);
    }

    @Test
    void shouldDeleteOrgWhenNotInUse() {
        UUID id = UUID.randomUUID();
        when(repository.hasDependencies(id)).thenReturn(false);

        service.deleteOrganization(id);
        verify(repository).delete(id);
    }
}
