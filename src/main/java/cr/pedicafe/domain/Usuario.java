package cr.pedicafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "contrasenaHash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

 
    @ManyToOne(optional = false)
    @JoinColumn(name = "idRol", nullable = false)
    private Rol rol;

  
    @Column(name = "fechaCreacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fechaModificacion", insertable = false, updatable = false)
    private LocalDateTime fechaModificacion;
}
