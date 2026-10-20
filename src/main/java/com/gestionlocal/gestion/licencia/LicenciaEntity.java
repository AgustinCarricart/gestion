package com.gestionlocal.gestion.licencia;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "configuracion_licencia")
public class LicenciaEntity {

    @Id
    private Long id; // ID 1 fijo (registro singleton)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanEnum plan;

    @Column(name = "nombre_cliente", nullable = false)
    private String nombreCliente;

    @Column(nullable = false)
    private boolean activo; // false = suspendido por falta de pago

    @Column(name = "feature_telegram", nullable = false)
    private boolean featureTelegram;

    @Column(name = "feature_impresora_termica", nullable = false)
    private boolean featureImpresoraTermica;

    @Column(name = "feature_resumen_lotes", nullable = false)
    private boolean featureResumenLotes;

    @Column(name = "feature_sse_realtime", nullable = false)
    private boolean featureSseRealtime;

    @Column(name = "feature_costos_recetas", nullable = false)
    private boolean featureCostosRecetas;

    @Column(name = "notas", length = 1000)
    private String notas;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
