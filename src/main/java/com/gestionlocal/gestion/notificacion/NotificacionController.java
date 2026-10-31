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

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
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
}
