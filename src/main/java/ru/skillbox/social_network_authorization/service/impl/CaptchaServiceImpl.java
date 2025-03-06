package ru.skillbox.social_network_authorization.service.impl;

import com.github.cage.Cage;
import com.github.cage.GCage;
import ru.skillbox.social_network_authorization.dto.CaptchaDto;
import ru.skillbox.social_network_authorization.exception.CaptchaException;
import ru.skillbox.social_network_authorization.service.CaptchaService;
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
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            // Рисуем капчу в поток байтов
            cage.draw(secret, os);
        } catch (IOException e) {
            e.printStackTrace();
            // Выводим стек трейс для анализа
            throw new CaptchaException();
        }

        //Временная заглушка для капчи
        secret = "";

        String image = Base64.getEncoder().encodeToString(os.toByteArray());
        // Создаем объект, содержащий токен и данные изображения
        return new CaptchaDto(secret, image);
    }
}
