package com.gestionlocal.gestion.licencia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenciaUpdateRequest {
    private PlanEnum plan;
    private String nombreCliente;
    private Boolean activo;
    private Boolean featureTelegram;
    private Boolean featureImpresoraTermica;
    private Boolean featureResumenLotes;
    private Boolean featureSseRealtime;
    private Boolean featureCostosRecetas;
    private String notas;
}
