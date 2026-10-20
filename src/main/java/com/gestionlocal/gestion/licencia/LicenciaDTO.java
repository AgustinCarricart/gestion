package com.gestionlocal.gestion.licencia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenciaDTO {
    private PlanEnum plan;
    private String nombreCliente;
    private boolean activo;
    private boolean featureTelegram;
    private boolean featureImpresoraTermica;
    private boolean featureResumenLotes;
    private boolean featureSseRealtime;
    private boolean featureCostosRecetas;
    private String notas;
    private LocalDateTime actualizadoEn;
}
