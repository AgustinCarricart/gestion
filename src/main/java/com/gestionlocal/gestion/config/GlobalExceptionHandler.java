package com.gestionlocal.gestion.config;

import com.gestionlocal.gestion.cliente.exception.ClienteAlreadyExists;
import com.gestionlocal.gestion.cliente.exception.ClienteConPedidosActivosException;
import com.gestionlocal.gestion.cliente.exception.ClienteInactivoException;
import com.gestionlocal.gestion.cliente.exception.ClienteNotFoundException;
import com.gestionlocal.gestion.pedido.exception.CantidadInvalidaException;
import com.gestionlocal.gestion.pedido.exception.EstadoTransicionInvalidaException;
import com.gestionlocal.gestion.pedido.exception.PedidoNotFoundException;
import com.gestionlocal.gestion.pedido.exception.PesoInvalidoException;
import com.gestionlocal.gestion.producto.exception.ProductoAlreadyExists;
import com.gestionlocal.gestion.producto.exception.ProductoConPedidosActivosException;
import com.gestionlocal.gestion.producto.exception.ProductoInactivoException;
import com.gestionlocal.gestion.producto.exception.ProductoNotFoundException;
import com.gestionlocal.gestion.usuario.exceptions.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    // --- 404 Not Found ---

    @ExceptionHandler({ClienteNotFoundException.class, ProductoNotFoundException.class, PedidoNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex)
    {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- 409 Conflict ---


    @ExceptionHandler({ClienteAlreadyExists.class, ProductoAlreadyExists.class, UsernameAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handleAlreadyExists(RuntimeException ex)
    {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- 400 Bad Request ---

    @ExceptionHandler({
            ClienteInactivoException.class,
            ClienteConPedidosActivosException.class,
            ProductoInactivoException.class,
            EstadoTransicionInvalidaException.class,
            CantidadInvalidaException.class,
            PesoInvalidoException.class,
            IllegalArgumentException.class,
            ProductoConPedidosActivosException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex)
    {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- 400 Validation errors (@Valid) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex)
    {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));

        ValidationErrorResponse error = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                errores
        );

        return ResponseEntity.badRequest().body(error);
    }

    // --- Helper ---

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String mensaje)
    {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), status.value(), mensaje);
        return ResponseEntity.status(status).body(error);
    }
}