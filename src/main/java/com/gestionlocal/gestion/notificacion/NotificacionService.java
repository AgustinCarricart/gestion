package com.gestionlocal.gestion.notificacion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestionlocal.gestion.pedido.DetallePedidosEntity;
import com.gestionlocal.gestion.pedido.PedidoEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
    private static final String CLAVE_TELEGRAM_TOKEN = "TELEGRAM_BOT_TOKEN";
    private static final String CLAVE_TELEGRAM_CHAT_ID = "TELEGRAM_CHAT_ID";

    private final ConfiguracionRepository configuracionRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public NotificacionService(ConfiguracionRepository configuracionRepository, ObjectMapper objectMapper) {
        this.configuracionRepository = configuracionRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public SseEmitter suscribir(String username) {
        // 30 minutos de timeout
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitters.add(emitter);
        log.info("Nuevo cliente SSE conectado (Usuario: {}). Total conectados: {}", username, emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("Cliente SSE desconectado (completado). Total: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.info("Cliente SSE desconectado (timeout). Total: {}", emitters.size());
        });

        emitter.onError(e -> {
            emitters.remove(emitter);
            log.debug("Error en conexión SSE: {}", e.getMessage());
        });

        // Enviar evento inicial de conexión
        try {
            emitter.send(SseEmitter.event()
                    .name("conectado")
                    .data(Map.of("mensaje", "Conectado al canal de notificaciones en vivo", "timestamp", System.currentTimeMillis())));
        } catch (Exception e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    public void notificarNuevoPedido(PedidoEntity pedido) {
        final String resumenProductos = (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty())
                ? pedido.getDetalles().stream()
                    .map(d -> {
                        String nombre = d.getProducto() != null ? d.getProducto().getNombreProducto() : "Producto";
                        String cant = d.getCantidad() != null ? d.getCantidad() + "x " : "";
                        String kg = d.getPesoKg() != null ? " (" + d.getPesoKg() + " kg)" : "";
                        return "• " + cant + nombre + kg;
                    })
                    .collect(Collectors.joining("\n"))
                : "";

        Double sena = pedido.getSena() != null ? pedido.getSena() : 0.0;
        Double total = pedido.getTotal() != null ? pedido.getTotal() : 0.0;
        Double saldo = Math.max(0.0, total - sena);

        AlertaNuevoPedidoDto alerta = AlertaNuevoPedidoDto.builder()
                .tipo("NUEVO_PEDIDO")
                .pedidoPublicId(pedido.getPublicId())
                .nombreCliente(pedido.getNombreCliente())
                .telefonoCliente(pedido.getTelefonoCliente())
                .fechaEntrega(pedido.getFechaEntrega())
                .total(total)
                .sena(sena)
                .saldoPendiente(saldo)
                .resumenProductos(resumenProductos)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();

        // 1. Emitir SSE a navegadores y PWA activos
        emitirEventoAClientes("nuevo_pedido", alerta);

        // 2. Enviar a Telegram en segundo plano
        CompletableFuture.runAsync(() -> enviarTelegramNuevoPedido(pedido, resumenProductos, total, sena, saldo));
    }

    private void emitirEventoAClientes(String evento, Object data) {
        List<SseEmitter> muertos = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(evento)
                        .data(data));
            } catch (Exception e) {
                muertos.add(emitter);
            }
        }
        if (!muertos.isEmpty()) {
            emitters.removeAll(muertos);
        }
    }

    private void enviarTelegramNuevoPedido(PedidoEntity pedido, String resumenProductos, Double total, Double sena, Double saldo) {
        String token = obtenerTokenTelegram();
        String chatId = obtenerChatIdTelegram();

        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            log.debug("Telegram no configurado. Omitiendo notificación.");
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaStr = pedido.getFechaEntrega() != null ? pedido.getFechaEntrega().format(dtf) + " hs" : "A coordinar";

        StringBuilder sb = new StringBuilder();
        sb.append("🍰 *¡NUEVO PEDIDO REGISTRADO!* 🍰\n\n");
        sb.append("👤 *Cliente:* ").append(pedido.getNombreCliente() != null ? pedido.getNombreCliente() : "Sin nombre").append("\n");
        if (pedido.getTelefonoCliente() != null && !pedido.getTelefonoCliente().isBlank()) {
            sb.append("📞 *Teléfono:* `").append(pedido.getTelefonoCliente()).append("`\n");
        }
        sb.append("📅 *Entrega:* ").append(fechaStr).append("\n\n");

        if (!resumenProductos.isBlank()) {
            sb.append("🧁 *Productos:*\n").append(resumenProductos).append("\n\n");
        }

        if (pedido.getObservaciones() != null && !pedido.getObservaciones().isBlank()) {
            sb.append("📝 *Observaciones:* _").append(pedido.getObservaciones()).append("_\n\n");
        }

        sb.append("💰 *Total:* $").append(String.format("%,.0f", total).replace(',', '.')).append("\n");
        sb.append("💵 *Seña abonada:* $").append(String.format("%,.0f", sena).replace(',', '.')).append("\n");
        sb.append("⚠️ *Saldo a cobrar:* $").append(String.format("%,.0f", saldo).replace(',', '.')).append("\n\n");
        sb.append("👉 _Accedé a la app para gestionarlo: https://frontend-gestion.vercel.app_");

        ejecutarEnvioTelegram(token, chatId, sb.toString());
    }

    public boolean enviarPruebaTelegram() {
        String token = obtenerTokenTelegram();
        String chatId = obtenerChatIdTelegram();

        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            throw new IllegalStateException("El Token o Chat ID de Telegram no están configurados.");
        }

        String mensaje = "🍰 *Tortas Olivia - Alerta de Prueba* 🔔\n\n"
                + "¡El bot de notificaciones está funcionando correctamente! A partir de ahora recibirás aquí los avisos en tiempo real cada vez que se cargue un nuevo pedido en el sistema.";

        return ejecutarEnvioTelegram(token, chatId, mensaje);
    }

    private boolean ejecutarEnvioTelegram(String token, String chatId, String textoMarkdown) {
        try {
            String url = "https://api.telegram.org/bot" + token + "/sendMessage";
            Map<String, Object> body = Map.of(
                    "chat_id", chatId,
                    "text", textoMarkdown,
                    "parse_mode", "Markdown"
            );

            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                log.info("Mensaje enviado exitosamente a Telegram.");
                return true;
            } else {
                log.error("Telegram API devolvió status {}: {}", response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("Error al enviar mensaje a Telegram: {}", e.getMessage());
            return false;
        }
    }

    public NotificacionConfigDto getConfig() {
        String token = obtenerTokenTelegram();
        String chatId = obtenerChatIdTelegram();
        boolean activo = token != null && !token.isBlank() && chatId != null && !chatId.isBlank();
        // Ocultar la mitad del token por privacidad si existe
        String tokenMasked = token;
        if (token != null && token.length() > 8) {
            tokenMasked = token.substring(0, 4) + "••••••••" + token.substring(token.length() - 4);
        }
        return new NotificacionConfigDto(tokenMasked, chatId, activo);
    }

    public void guardarConfig(NotificacionConfigDto dto) {
        if (dto.getTelegramToken() != null && !dto.getTelegramToken().isBlank() && !dto.getTelegramToken().contains("••••")) {
            guardarValor(CLAVE_TELEGRAM_TOKEN, dto.getTelegramToken().trim());
        }
        if (dto.getTelegramChatId() != null) {
            guardarValor(CLAVE_TELEGRAM_CHAT_ID, dto.getTelegramChatId().trim());
        }
    }

    private String obtenerTokenTelegram() {
        String env = System.getenv("TELEGRAM_BOT_TOKEN");
        if (env != null && !env.isBlank()) return env;
        return configuracionRepository.findByClave(CLAVE_TELEGRAM_TOKEN)
                .map(ConfiguracionEntity::getValor)
                .orElse(null);
    }

    private String obtenerChatIdTelegram() {
        String env = System.getenv("TELEGRAM_CHAT_ID");
        if (env != null && !env.isBlank()) return env;
        return configuracionRepository.findByClave(CLAVE_TELEGRAM_CHAT_ID)
                .map(ConfiguracionEntity::getValor)
                .orElse(null);
    }

    private void guardarValor(String clave, String valor) {
        ConfiguracionEntity entity = configuracionRepository.findByClave(clave)
                .orElse(new ConfiguracionEntity(clave, valor));
        entity.setValor(valor);
        configuracionRepository.save(entity);
    }
}
