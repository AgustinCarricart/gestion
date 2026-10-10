package com.gestionlocal.gestion.notificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionConfigDto {
    private String telegramToken;
    private String telegramChatId;
    private boolean telegramActivo;
}
