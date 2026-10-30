package cr.pedicafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCategoria")
    private Integer idCategoria;

    @NotBlank(message = "{validation.category.name.required}")
    @Size(max = 80, message = "{validation.category.name.size}")
    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;

    @Size(max = 255, message = "{validation.category.description.size}")
    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

}
