package com.gestionlocal.gestion.licencia;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LicenciaService {

    private static final Logger log = LoggerFactory.getLogger(LicenciaService.class);
    private static final Long LICENCIA_ID = 1L;

    private final LicenciaRepository licenciaRepository;

    @Value("${app.plan:PRO}")
    private String defaultPlan;

    @Value("${app.cliente.nombre:Tortas Olivia}")
    private String defaultNombreCliente;

    public LicenciaService(LicenciaRepository licenciaRepository) {
        this.licenciaRepository = licenciaRepository;
    }

    @PostConstruct
    @Transactional
    public void inicializarLicenciaSiNoExiste() {
        if (!licenciaRepository.existsById(LICENCIA_ID)) {
            PlanEnum planInicial;
            try {
                planInicial = PlanEnum.valueOf(defaultPlan.trim().toUpperCase());
            } catch (Exception e) {
                planInicial = PlanEnum.PRO;
            }

            LicenciaEntity entidad = new LicenciaEntity();
            entidad.setId(LICENCIA_ID);
            entidad.setPlan(planInicial);
            entidad.setNombreCliente(defaultNombreCliente);
            entidad.setActivo(true);
            entidad.setNotas("Licencia inicial autogenerada para el comercio.");

            // Configurar flags por defecto según el plan inicial
            configurarFeaturesSegunPlan(entidad, planInicial);

            licenciaRepository.save(entidad);
            log.info("Licencia inicial creada: Plan='{}', Cliente='{}', Activo=true", planInicial, defaultNombreCliente);
        }
    }

    @Transactional(readOnly = true)
    public LicenciaDTO obtenerLicencia() {
        LicenciaEntity entidad = licenciaRepository.findById(LICENCIA_ID)
                .orElseGet(() -> {
                    LicenciaEntity fallback = new LicenciaEntity();
                    fallback.setId(LICENCIA_ID);
                    fallback.setPlan(PlanEnum.PRO);
                    fallback.setNombreCliente(defaultNombreCliente);
                    fallback.setActivo(true);
                    configurarFeaturesSegunPlan(fallback, PlanEnum.PRO);
                    return fallback;
                });

        return mapearADto(entidad);
    }

    @Transactional
    public LicenciaDTO actualizarLicencia(LicenciaUpdateRequest req) {
        LicenciaEntity entidad = licenciaRepository.findById(LICENCIA_ID)
                .orElseGet(() -> {
                    LicenciaEntity e = new LicenciaEntity();
                    e.setId(LICENCIA_ID);
                    e.setPlan(PlanEnum.PRO);
                    e.setNombreCliente(defaultNombreCliente);
                    e.setActivo(true);
                    return e;
                });

        if (req.getPlan() != null && req.getPlan() != entidad.getPlan()) {
            entidad.setPlan(req.getPlan());
            // Si cambia el plan y no especificó flags en el request, adoptar los defaults del nuevo plan
            if (req.getFeatureTelegram() == null && req.getFeatureImpresoraTermica() == null) {
                configurarFeaturesSegunPlan(entidad, req.getPlan());
            }
        }

        if (req.getNombreCliente() != null && !req.getNombreCliente().isBlank()) {
            entidad.setNombreCliente(req.getNombreCliente().trim());
        }
        if (req.getActivo() != null) {
            entidad.setActivo(req.getActivo());
        }
        if (req.getFeatureTelegram() != null) {
            entidad.setFeatureTelegram(req.getFeatureTelegram());
        }
        if (req.getFeatureImpresoraTermica() != null) {
            entidad.setFeatureImpresoraTermica(req.getFeatureImpresoraTermica());
        }
        if (req.getFeatureResumenLotes() != null) {
            entidad.setFeatureResumenLotes(req.getFeatureResumenLotes());
        }
        if (req.getFeatureSseRealtime() != null) {
            entidad.setFeatureSseRealtime(req.getFeatureSseRealtime());
        }
        if (req.getFeatureCostosRecetas() != null) {
            entidad.setFeatureCostosRecetas(req.getFeatureCostosRecetas());
        }
        if (req.getNotas() != null) {
            entidad.setNotas(req.getNotas());
        }

        LicenciaEntity guardada = licenciaRepository.save(entidad);
        log.info("Licencia actualizada por SuperAdmin: Plan='{}', Activo={}, Cliente='{}'",
                guardada.getPlan(), guardada.isActivo(), guardada.getNombreCliente());

        return mapearADto(guardada);
    }

    private void configurarFeaturesSegunPlan(LicenciaEntity entidad, PlanEnum plan) {
        switch (plan) {
            case STARTER:
                entidad.setFeatureTelegram(false);
                entidad.setFeatureImpresoraTermica(false);
                entidad.setFeatureResumenLotes(false);
                entidad.setFeatureSseRealtime(false);
                entidad.setFeatureCostosRecetas(false);
                break;
            case PRO:
                entidad.setFeatureTelegram(true);
                entidad.setFeatureImpresoraTermica(true);
                entidad.setFeatureResumenLotes(true);
                entidad.setFeatureSseRealtime(true);
                entidad.setFeatureCostosRecetas(false);
                break;
            case ENTERPRISE:
                entidad.setFeatureTelegram(true);
                entidad.setFeatureImpresoraTermica(true);
                entidad.setFeatureResumenLotes(true);
                entidad.setFeatureSseRealtime(true);
                entidad.setFeatureCostosRecetas(true);
                break;
        }
    }

    private LicenciaDTO mapearADto(LicenciaEntity e) {
        return LicenciaDTO.builder()
                .plan(e.getPlan())
                .nombreCliente(e.getNombreCliente())
                .activo(e.isActivo())
                .featureTelegram(e.isFeatureTelegram())
                .featureImpresoraTermica(e.isFeatureImpresoraTermica())
                .featureResumenLotes(e.isFeatureResumenLotes())
                .featureSseRealtime(e.isFeatureSseRealtime())
                .featureCostosRecetas(e.isFeatureCostosRecetas())
                .notas(e.getNotas())
                .actualizadoEn(e.getActualizadoEn())
                .build();
    }
}
