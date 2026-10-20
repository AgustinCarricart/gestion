package com.gestionlocal.gestion.licencia;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/licencia")
public class LicenciaController {

    private final LicenciaService licenciaService;

    public LicenciaController(LicenciaService licenciaService) {
        this.licenciaService = licenciaService;
    }

    /**
     * Endpoint público / para cualquier usuario autenticado:
     * Devuelve el estado de la licencia y los features habilitados para adaptar la UI.
     */
    @GetMapping("/public")
    public ResponseEntity<LicenciaDTO> obtenerLicenciaPublica() {
        return ResponseEntity.ok(licenciaService.obtenerLicencia());
    }

    /**
     * Endpoint exclusivo de SUPERADMIN:
     * Consulta los detalles completos de la licencia.
     */
    @GetMapping
    public ResponseEntity<LicenciaDTO> obtenerLicenciaCompleta() {
        return ResponseEntity.ok(licenciaService.obtenerLicencia());
    }

    /**
     * Endpoint exclusivo de SUPERADMIN:
     * Actualiza el plan, activa/desactiva módulos o suspende/reactiva el servicio.
     */
    @PutMapping
    public ResponseEntity<LicenciaDTO> actualizarLicencia(@RequestBody LicenciaUpdateRequest request) {
        return ResponseEntity.ok(licenciaService.actualizarLicencia(request));
    }
}
