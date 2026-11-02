package cr.pedicafe.service;

import cr.pedicafe.domain.Usuario;
import cr.pedicafe.repository.UsuarioRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        String correoNormalizado = correo == null
                ? "" : correo.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase(correoNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No existe un usuario con ese correo."));

       
        return User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .roles(usuario.getRol().getNombre().toUpperCase(Locale.ROOT))
                .disabled(!usuario.isActivo())
                .build();
    }
}
