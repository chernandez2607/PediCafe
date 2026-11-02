package cr.pedicafe.service;

import cr.pedicafe.domain.FormularioRegistro;
import cr.pedicafe.domain.Rol;
import cr.pedicafe.domain.Usuario;
import cr.pedicafe.repository.RolRepository;
import cr.pedicafe.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios() {
        return usuarioRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreoIgnoreCase(normalizarCorreo(correo));
    }

   
    @Transactional
    public Usuario registrar(FormularioRegistro formulario) {
        String correo = normalizarCorreo(formulario.getCorreo());

        if (!formulario.getContrasena().equals(formulario.getConfirmarContrasena())) {
            throw new IllegalArgumentException("user.password.mismatch");
        }
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalArgumentException("user.email.duplicate");
        }

        
        Rol rolCliente = rolRepository.findByNombre(Rol.CLIENTE)
                .orElseThrow(() -> new IllegalStateException("role.client.missing"));

        Usuario usuario = new Usuario();
        usuario.setNombre(formulario.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setContrasenaHash(passwordEncoder.encode(formulario.getContrasena()));
        usuario.setActivo(true);
        usuario.setRol(rolCliente);

        try {
            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            // Respaldo: la base tiene UNIQUE en correo.
            throw new IllegalArgumentException("user.email.duplicate");
        }
    }

    @Transactional
    public void actualizarNombre(String correo, String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("validation.user.name.required");
        }
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("validation.user.name.size");
        }
        Usuario usuario = getUsuarioPorCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("user.notFound"));
        usuario.setNombre(nombre.trim());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarContrasena(String correo, String actual,
                                  String nueva, String confirmacion) {
        Usuario usuario = getUsuarioPorCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("user.notFound"));

        if (actual == null || !passwordEncoder.matches(actual, usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("user.password.current.invalid");
        }
        if (nueva == null || nueva.length() < 8 || nueva.length() > 72) {
            throw new IllegalArgumentException("validation.user.password.size");
        }
        if (!nueva.equals(confirmacion)) {
            throw new IllegalArgumentException("user.password.mismatch");
        }
        usuario.setContrasenaHash(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);
    }

   
    @Transactional
    public void actualizarRolYEstado(Integer idUsuario, Integer idRol,
                                     boolean activo, String correoAdministrador) {
        Usuario usuario = getUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("user.notFound"));
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("role.notFound"));

        boolean esSuPropiaCuenta = usuario.getCorreo().equalsIgnoreCase(correoAdministrador);
        if (esSuPropiaCuenta && (!activo || !Rol.ADMINISTRADOR.equals(rol.getNombre()))) {
            throw new IllegalStateException("user.self.change");
        }

        usuario.setRol(rol);
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
