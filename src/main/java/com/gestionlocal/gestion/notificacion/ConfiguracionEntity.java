package com.gestionlocal.gestion.notificacion;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuraciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave", unique = true, nullable = false)
    private String clave;

    @Column(name = "valor", length = 1000)
    private String valor;

    public ConfiguracionEntity(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }
}
