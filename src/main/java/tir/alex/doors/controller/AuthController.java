package tir.alex.doors.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tir.alex.doors.service.AuthService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Контроллер для авторизации пользователей")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/send-code")
    @Operation(summary = "Отправка кода подтверждения", description = "Генерирует и отправляет код на телефон через SMS.ru")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код успешно отправлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка при отправке кода")
    })
    public ResponseEntity<String> sendCode(@RequestParam String phone) {
        return authService.sendCode(phone);
    }

    @PostMapping("/verify-code")
    @Operation(summary = "Подтверждение кода", description = "Проверяет введенный код и, если он верный, возвращает JWT токен")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код верный, токен выдан"),
            @ApiResponse(responseCode = "400", description = "Неверный код")
    })
    public ResponseEntity<String> verifyCode(@RequestParam String phone, @RequestParam String code) {
        return authService.verifyCode(phone, code);
    }
}
