package com.gestionlocal.gestion.notificacion;

import com.gestionlocal.gestion.pedido.DetallePedidosEntity;
import com.gestionlocal.gestion.pedido.EstadoPedido;
import com.gestionlocal.gestion.pedido.PedidoEntity;
import com.gestionlocal.gestion.pedido.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Resumen matutino diario enviado por Telegram a las 9:00 AM (hora de Argentina).
 * Combina datos operativos en vivo de Tortas Olivia (pedidos de hoy, lote de producción,
 * saldos a cobrar, pedidos de mañana) con la lista de pendientes de todos los proyectos activos.
 */
@Service
public class ResumenMatutinoService {

    private static final Logger log = LoggerFactory.getLogger(ResumenMatutinoService.class);
    static final String CLAVE_PENDIENTES = "RESUMEN_PENDIENTES_PROYECTOS";
    static final int MAX_PENDIENTES = 1000;
    private static final ZoneId ZONA_AR = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final List<EstadoPedido> ESTADOS_ACTIVOS = List.of(EstadoPedido.PENDIENTE, EstadoPedido.LISTO);

    static final String PENDIENTES_POR_DEFECTO = String.join("\n",
            "🍰 Olivia:",
            "• Aviso por WhatsApp al cliente cuando el pedido está LISTO",
            "• Link de pago de señas con Mercado Pago",
            "• Fase 1 de Costos/Escandallos (carga de insumos)",
            "",
            "☕ Cafetería MDP:",
            "• Contactar 2-3 tostadores (muestras y precios)",
            "• Cata / taller de barismo",
            "• Elegir Top 5 de pastelería al paso",
            "• Relevar locales (Güemes, Tribunales, Plaza Mitre)",
            "",
            "🌐 Sinergias:",
            "• Combos café + porción Olivia",
            "• Canal mayorista interno Olivia → Cafetería",
            "",
            "🎯 Sugerencia de hoy: Aviso por WhatsApp al cliente (rápido y de alto impacto)");

    private final PedidoRepository pedidoRepository;
    private final ConfiguracionRepository configuracionRepository;
    private final NotificacionService notificacionService;

    public ResumenMatutinoService(PedidoRepository pedidoRepository,
                                  ConfiguracionRepository configuracionRepository,
                                  NotificacionService notificacionService) {
        this.pedidoRepository = pedidoRepository;
        this.configuracionRepository = configuracionRepository;
        this.notificacionService = notificacionService;
    }

    /** Todos los días a las 9:00 AM hora de Argentina. */
    @Scheduled(cron = "0 0 9 * * *", zone = "America/Argentina/Buenos_Aires")
    public void enviarResumenProgramado() {
        log.info("Ejecutando envío programado del resumen matutino...");
        boolean ok = enviarAhora();
        log.info("Resumen matutino {}", ok ? "enviado correctamente" : "NO enviado (revisar configuración de Telegram)");
    }

    @Transactional(readOnly = true)
    public boolean enviarAhora() {
        return notificacionService.enviarMensajeTelegram(construirMensaje(LocalDate.now(ZONA_AR)));
    }

    public String obtenerPendientes() {
        return configuracionRepository.findByClave(CLAVE_PENDIENTES)
                .map(ConfiguracionEntity::getValor)
                .filter(v -> !v.isBlank())
                .orElse(PENDIENTES_POR_DEFECTO);
    }

    public void guardarPendientes(String texto) {
        String valor = texto == null ? "" : texto.trim();
        if (valor.length() > MAX_PENDIENTES) {
            throw new IllegalArgumentException("El texto de pendientes no puede superar los " + MAX_PENDIENTES + " caracteres.");
        }
        ConfiguracionEntity entity = configuracionRepository.findByClave(CLAVE_PENDIENTES)
                .orElse(new ConfiguracionEntity(CLAVE_PENDIENTES, valor));
        entity.setValor(valor);
        configuracionRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public String construirMensaje(LocalDate hoy) {
        List<PedidoEntity> pedidosHoy = pedidosDelDia(hoy);
        List<PedidoEntity> pedidosManana = pedidosDelDia(hoy.plusDays(1));

        StringBuilder sb = new StringBuilder();
        String diaSemana = hoy.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("es", "AR"));
        diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);
        sb.append("☀️ *¡Buen día, Agustín!* — ").append(diaSemana).append(" ")
          .append(hoy.getDayOfMonth()).append("/").append(hoy.getMonthValue()).append("\n\n");

        // ── Operación de hoy en Tortas Olivia ──
        sb.append("🍰 *Tortas Olivia — Hoy*\n");
        if (pedidosHoy.isEmpty()) {
            sb.append("• Sin pedidos agendados para hoy\n");
        } else {
            long pendientes = pedidosHoy.stream().filter(p -> p.getEstadoPedido() == EstadoPedido.PENDIENTE).count();
            long listos = pedidosHoy.size() - pendientes;
            double aCobrar = pedidosHoy.stream()
                    .mapToDouble(p -> Math.max(0.0, nz(p.getTotal()) - nz(p.getSena())))
                    .sum();
            sb.append("• Pedidos para hoy: ").append(pedidosHoy.size())
              .append(" (").append(pendientes).append(" a preparar, ").append(listos).append(" listos)\n");
            sb.append("• A cobrar en mostrador: $").append(formatoPesos(aCobrar)).append("\n");

            Map<String, double[]> lote = calcularLote(pedidosHoy);
            if (!lote.isEmpty()) {
                sb.append("• Lote de producción:\n");
                lote.forEach((nombre, v) -> {
                    sb.append("   - ").append((int) v[0]).append("x ").append(nombre);
                    if (v[1] > 0) sb.append(" (").append(String.format(Locale.US, "%.1f", v[1])).append(" kg)");
                    sb.append("\n");
                });
            }
        }
        sb.append("• Mañana: ").append(pedidosManana.size())
          .append(pedidosManana.size() == 1 ? " pedido agendado" : " pedidos agendados").append("\n\n");

        // ── Pendientes de todos los proyectos ──
        sb.append("📋 *Pendientes de tus proyectos*\n");
        sb.append(obtenerPendientes()).append("\n\n");

        sb.append("👉 Respondeme en Antigravity con \"OK\" + la tarea que querés encarar hoy y arranco.");
        return sb.toString();
    }

    private List<PedidoEntity> pedidosDelDia(LocalDate dia) {
        return pedidoRepository.findByEstadoPedidoInAndFechaEntregaBetweenOrderByFechaEntregaAsc(
                ESTADOS_ACTIVOS, dia.atStartOfDay(), dia.atTime(23, 59, 59));
    }

    /** Agrupa cantidades y kilos por producto. Valor: [cantidad, kg]. */
    private Map<String, double[]> calcularLote(List<PedidoEntity> pedidos) {
        Map<String, double[]> lote = new TreeMap<>();
        for (PedidoEntity p : pedidos) {
            if (p.getDetalles() == null) continue;
            for (DetallePedidosEntity d : p.getDetalles()) {
                String nombre = (d.getProducto() != null && d.getProducto().getNombreProducto() != null)
                        ? d.getProducto().getNombreProducto() : "Producto";
                double[] acc = lote.computeIfAbsent(nombre, k -> new double[2]);
                acc[0] += d.getCantidad() != null ? d.getCantidad() : 0;
                acc[1] += d.getPesoKg() != null ? d.getPesoKg() : 0;
            }
        }
        return lote;
    }

    private static double nz(Double v) {
        return v != null ? v : 0.0;
    }

    private static String formatoPesos(double monto) {
        return String.format(Locale.US, "%,.0f", monto).replace(',', '.');
    }
}
