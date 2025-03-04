package tir.alex.doors.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tir.alex.doors.Config.SmsConfig;
import tir.alex.doors.entity.User;
import tir.alex.doors.service.AuthService;
import tir.alex.doors.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SmsConfig smsConfig;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;
    @Value("${auth.smsru-api-id}")
    private String smsruApiId;

    @Value("${auth.jwt-secret}")
    private String jwtSecret;

    @Override
    public ResponseEntity<String> sendCode(String phone) {
        String code;

        if (smsConfig.getTestNumbers().contains(phone)) {
            code = "1111";
        } else {
            String response = sendRequestToSmsRu(phone);
            if (response == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при отправке кода");
            }

            code = extractCodeFromResponse(response);
            if (code == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при разборе ответа SMS.RU");
            }
        }

        redisTemplate.opsForValue().set("sms_code:" + phone, code, 5, TimeUnit.MINUTES);
        return ResponseEntity.ok("Код отправлен");
    }
    @Override
    public ResponseEntity<String> verifyCode(String phone, String code) {
        String storedCode = redisTemplate.opsForValue().get("sms_code:" + phone);

        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.delete("sms_code:" + phone);
            User user = userService.findByPhoneNumber(phone);
            if (user == null) {
                user = userService.createUser(phone);
            }
            String role = user.getRole();
            if (!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
            String token = Jwts.builder()
                    .setSubject(phone)
                    .claim("role", role)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes(StandardCharsets.UTF_8))
                    .compact();
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.badRequest().body("Неверный код");
    }

    private String sendRequestToSmsRu(String phone) {
        String url = String.format("https://sms.ru/code/call?api_id=%s&phone=%s", smsruApiId, phone);
        return new RestTemplate().getForObject(url, String.class);
    }

    private String extractCodeFromResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);
            if (rootNode.has("status") && "OK".equals(rootNode.get("status").asText())) {
                return rootNode.has("code") ? rootNode.get("code").asText() : null;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
