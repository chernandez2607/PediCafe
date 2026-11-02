package cr.pedicafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    
    public static final String CLIENTE = "CLIENTE";
    public static final String BARISTA = "BARISTA";
    public static final String CAJERO = "CAJERO";
    public static final String ADMINISTRADOR = "ADMINISTRADOR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRol")
    private Integer idRol;

    @Column(name = "nombre", nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(name = "descripcion", length = 120)
    private String descripcion;
}