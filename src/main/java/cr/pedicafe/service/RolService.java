package cr.pedicafe.service;

import cr.pedicafe.domain.Rol;
import cr.pedicafe.repository.RolRepository;
import cr.pedicafe.repository.UsuarioRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Rol> getRoles() {
        return rolRepository.findAllByOrderByIdRolAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Rol> getRol(Integer idRol) {
        return rolRepository.findById(idRol);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getCantidadUsuariosPorRol() {
        Map<Integer, Long> cantidades = new LinkedHashMap<>();
        for (Rol rol : getRoles()) {
            cantidades.put(rol.getIdRol(), usuarioRepository.countByRolIdRol(rol.getIdRol()));
        }
        return cantidades;
    }
}
