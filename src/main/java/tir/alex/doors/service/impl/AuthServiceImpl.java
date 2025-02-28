package tir.alex.doors.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tir.alex.doors.Config.SmsConfig;
import tir.alex.doors.service.AuthService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private final SmsConfig smsConfig;
    private final StringRedisTemplate redisTemplate;

    @Value("${auth.smsru-api-id}")
    private String smsruApiId;

    @Value("${auth.jwt-secret}")
    private String jwtSecret;

    public AuthServiceImpl(SmsConfig smsConfig, StringRedisTemplate redisTemplate) {
        this.smsConfig = smsConfig;
        this.redisTemplate = redisTemplate;
    }

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
            return ResponseEntity.ok(generateJwtToken(phone));
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

    private String generateJwtToken(String phone) {
        return Jwts.builder()
                .setSubject(phone)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }
}
