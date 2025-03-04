package tir.alex.doors.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tir.alex.doors.entity.UserPrincipal;

@RestController
@RequestMapping("/user")
@Tag(name = "User", description = "Контроллер для работы с пользователями")
public class UserController {

    @GetMapping("/me")
    @Operation(summary = "Получить информацию о текущем пользователе")
    public ResponseEntity<UserPrincipal> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(userPrincipal);
    }
}
