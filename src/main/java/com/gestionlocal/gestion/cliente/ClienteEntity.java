package com.gestionlocal.gestion.cliente;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "clientes")
public class ClienteEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="public_id", nullable = false,unique = true,updatable = false)
    private UUID publicId;

    @Column(name = "nombre",nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "num_telefono" , nullable = false)
    private String numero;

    @PrePersist
    private void generateValue()
    {
        if(this.publicId == null)
        {
            this.setPublicId(UUID.randomUUID());

        }

    }

}
