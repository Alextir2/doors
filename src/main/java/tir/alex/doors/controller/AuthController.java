package tir.alex.doors.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tir.alex.doors.Config.SmsConfig;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Контроллер для авторизации пользователей")
public class AuthController {

    @Autowired
    private SmsConfig smsConfig;

    private final StringRedisTemplate redisTemplate;
    @Value("${auth.smsru-api-id}")
    private String smsruApiId;

    @Value("${auth.jwt-secret}")
    private String jwtSecret;

    public AuthController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/send-code")
    @Operation(summary = "Отправка кода подтверждения", description = "Генерирует и отправляет код на телефон через SMS.ru")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код успешно отправлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка при отправке кода")
    })
    public ResponseEntity<String> sendCode(@RequestParam String phone) {
        String code;
        if (smsConfig.getTestNumbers().contains(phone)) {
            code = "1111";
        } else {
            String url = String.format("https://sms.ru/code/call?api_id=%s&phone=%s", smsruApiId, phone);
            String response = new RestTemplate().getForObject(url, String.class);
            if (response == null || !response.contains("\"status\":\"OK\"")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при отправке кода");
            }
            code = response.split("\"code\":\"")[1].split("\"")[0];
        }
        redisTemplate.opsForValue().set("sms_code:" + phone, code, 5, TimeUnit.MINUTES);
        return ResponseEntity.ok("Код отправлен");
    }


    @PostMapping("/verify-code")
    @Operation(summary = "Подтверждение кода", description = "Проверяет введенный код и, если он верный, возвращает JWT токен")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код верный, токен выдан"),
            @ApiResponse(responseCode = "400", description = "Неверный код")
    })
    public ResponseEntity<String> verifyCode(@RequestParam String phone, @RequestParam String code) {
        String storedCode = redisTemplate.opsForValue().get("sms_code:" + phone);
        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete("sms_code:" + phone);
            String token = Jwts.builder()
                    .setSubject(phone)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(SignatureAlgorithm.HS256, getSigningKey())
                    .compact();
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.badRequest().body("Неверный код");
    }

    private byte[] getSigningKey() {
        return jwtSecret.getBytes();
    }


}
