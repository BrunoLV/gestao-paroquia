package br.com.nsfatima.calendario.domain.policy;

import br.com.nsfatima.calendario.domain.type.PapelOrganizacional;
import br.com.nsfatima.calendario.domain.type.TipoOrganizacao;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationPolicy {

    public boolean isRoleAllowed(String organizationType, String role) {
        TipoOrganizacao normalizedOrg = TipoOrganizacao.fromStoredValue(organizationType);
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(role);

        if (normalizedOrg == null || normalizedRole == null) {
            return false;
        }

        return switch (normalizedOrg) {
            case PASTORAL, LAICATO -> normalizedRole == PapelOrganizacional.COORDENADOR
                    || normalizedRole == PapelOrganizacional.VICE_COORDENADOR
                    || normalizedRole == PapelOrganizacional.MEMBRO;
            case CLERO -> normalizedRole == PapelOrganizacional.PAROCO
                    || normalizedRole == PapelOrganizacional.VIGARIO
                    || normalizedRole == PapelOrganizacional.PADRE;
            case CONSELHO -> normalizedRole == PapelOrganizacional.COORDENADOR
                    || normalizedRole == PapelOrganizacional.VICE_COORDENADOR
                    || normalizedRole == PapelOrganizacional.SECRETARIO
                    || normalizedRole == PapelOrganizacional.MEMBRO;
        };
    }
}
