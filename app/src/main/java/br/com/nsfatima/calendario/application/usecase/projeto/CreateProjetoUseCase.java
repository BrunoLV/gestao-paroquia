package br.com.nsfatima.calendario.application.usecase.projeto;

import java.util.UUID;
import br.com.nsfatima.calendario.api.dto.projeto.ProjetoResponse;
import org.springframework.stereotype.Service;

@Service
public class CreateProjetoUseCase {

    public ProjetoResponse create(String nome, String descricao) {
        return new ProjetoResponse(
                UUID.randomUUID(),
                nome,
                descricao,
                false);
    }
}
