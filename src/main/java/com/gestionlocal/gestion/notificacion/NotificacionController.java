package com.gestionlocal.gestion.notificacion;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final ResumenMatutinoService resumenMatutinoService;

    public NotificacionController(NotificacionService notificacionService,
                                  ResumenMatutinoService resumenMatutinoService) {
        this.notificacionService = notificacionService;
        this.resumenMatutinoService = resumenMatutinoService;
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "Anon";
        return notificacionService.suscribir(username);
    }

    @GetMapping("/config")
    public ResponseEntity<NotificacionConfigDto> getConfig() {
        return ResponseEntity.ok(notificacionService.getConfig());
    }

    @PostMapping("/config")
    public ResponseEntity<Void> guardarConfig(@RequestBody NotificacionConfigDto dto) {
        notificacionService.guardarConfig(dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/telegram/test")
    public ResponseEntity<Map<String, Object>> probarTelegram() {
        boolean ok = notificacionService.enviarPruebaTelegram();
        if (ok) {
            return ResponseEntity.ok(Map.of("mensaje", "Alerta de prueba enviada exitosamente a Telegram"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo enviar el mensaje a Telegram. Verifica el Token y el Chat ID."));
        }
    }

    // ── Resumen matutino diario (9:00 AM) ──

    @GetMapping("/resumen-matutino")
    public ResponseEntity<Map<String, Object>> getResumenMatutino() {
        return ResponseEntity.ok(Map.of(
                "pendientes", resumenMatutinoService.obtenerPendientes(),
                "vistaPrevia", resumenMatutinoService.construirMensaje(java.time.LocalDate.now()),
                "telegramConfigurado", notificacionService.telegramConfigurado(),
                "horario", "Todos los días 09:00 (hora Argentina)"
        ));
    }

    @PutMapping("/resumen-matutino")
    public ResponseEntity<Map<String, Object>> guardarResumenMatutino(@RequestBody Map<String, String> body) {
        try {
            resumenMatutinoService.guardarPendientes(body != null ? body.get("pendientes") : "");
            return ResponseEntity.ok(Map.of("mensaje", "Pendientes del resumen matutino guardados"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }

    @PostMapping("/resumen-matutino/enviar")
    public ResponseEntity<Map<String, Object>> enviarResumenMatutino() {
        if (!notificacionService.telegramConfigurado()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Primero configurá el Token y el Chat ID de Telegram."));
        }
        boolean ok = resumenMatutinoService.enviarAhora();
        return ok
                ? ResponseEntity.ok(Map.of("mensaje", "Resumen matutino enviado a Telegram ✅"))
                : ResponseEntity.badRequest().body(Map.of("mensaje", "No se pudo enviar el resumen. Verificá el Token y el Chat ID."));
    }
}
