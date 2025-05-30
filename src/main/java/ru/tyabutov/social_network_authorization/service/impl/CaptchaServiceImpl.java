package ru.tyabutov.social_network_authorization.service.impl;

import com.github.cage.Cage;
import com.github.cage.GCage;
import ru.tyabutov.social_network_authorization.dto.CaptchaDto;
import ru.tyabutov.social_network_authorization.exception.CaptchaException;
import ru.tyabutov.social_network_authorization.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final Cage cage = new GCage();

    public CaptchaDto generateCaptcha() {
        // Генерация токена капчи
        String secret = cage.getTokenGenerator().next();

        // Уменьшение длины токена (не рекомендуется для капчи)
        int desiredLength = 6; // Желаемая длина токена
        if (secret.length() > desiredLength) {
            secret = secret.substring(0, desiredLength);
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            // Рисуем капчу в поток байтов
            cage.draw(secret, os);
        } catch (IOException e) {
            throw new CaptchaException();
        }

        String image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(os.toByteArray());

        // Создаем объект, содержащий токен и данные изображения
        return new CaptchaDto(secret, image);
    }
}