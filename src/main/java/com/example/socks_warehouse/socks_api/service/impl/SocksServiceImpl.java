package com.example.socks_warehouse.socks_api.service.impl;

import com.example.socks_warehouse.socks_api.dto.SocksDto;
import com.example.socks_warehouse.socks_api.model.Socks;
import com.example.socks_warehouse.socks_api.repository.SocksRepository;
import com.example.socks_warehouse.socks_api.service.SocksService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class SocksServiceImpl implements SocksService {
    private static final Logger logger = LoggerFactory.getLogger(SocksServiceImpl.class);

    private final SocksRepository socksRepository;

    public SocksServiceImpl(SocksRepository socksRepository) {
        this.socksRepository = socksRepository;
    }

    @Override
    public void registerIncome(SocksDto socksDto) {
        logger.info("Регистрация поступления носков: color={}, cottonPart={}, quantity={}",
                socksDto.getColor(), socksDto.getCottonPart(), socksDto.getQuantity());

        Socks socks = socksRepository.findByColorAndCottonPart(socksDto.getColor(), socksDto.getCottonPart())
                .orElse(new Socks());
        socks.setColor(socksDto.getColor().toLowerCase());
        socks.setCottonPart(socksDto.getCottonPart());
        socks.setQuantity(socks.getQuantity() + socksDto.getQuantity());

        socksRepository.save(socks);

        logger.info("Поступление успешно зарегистрировано.");
    }

    @Override
    public void registerOutcome(SocksDto socksDto) {
        logger.info("Регистрация уменьшения носков на складе: color={}, cottonPart={}. Уменьшаем на quantity={}",
                socksDto.getColor(), socksDto.getCottonPart(), socksDto.getQuantity());

        Socks socks = socksRepository.findByColorAndCottonPart(socksDto.getColor(), socksDto.getCottonPart())
                .orElseThrow(() -> new IllegalArgumentException("Носки такого цвета и содержания хлопка не найдены. " +
                        "Цвет = " + socksDto.getColor() + " содержание хлопка = " + socksDto.getCottonPart()));

        if (socks.getQuantity() < socksDto.getQuantity()) {
            throw new IllegalArgumentException(String.format("Недостаточное количество носков на складе. Остаток=%d, запрашивается=%d",
                    socks.getQuantity(), socksDto.getQuantity()));
        }

        socks.setQuantity(socks.getQuantity() - socksDto.getQuantity());
        socksRepository.save(socks);

        logger.info("Отпуск успешно зарегистрирован для: color={}, cottonPart={}, quantity={}",
                socksDto.getColor(), socksDto.getCottonPart(), socksDto.getQuantity());
    }


    @Override
    public int getTotalSocks(String color, String operation, int cottonPart) {
        logger.info("Запрос на получение общего количества носков: color={}, operation={}, cottonPart={}",
                color, operation, cottonPart);

        int totalSocks = switch (operation) {
            case "moreThan" -> socksRepository.findByColorAndCottonPartGreaterThan(color.toLowerCase(), cottonPart)
                    .stream()
                    .mapToInt(Socks::getQuantity)
                    .sum();
            case "lessThan" -> socksRepository.findByColorAndCottonPartLessThan(color.toLowerCase(), cottonPart)
                    .stream()
                    .mapToInt(Socks::getQuantity)
                    .sum();
            case "equal" -> socksRepository.findByColorAndCottonPart(color.toLowerCase(), cottonPart)
                    .map(Socks::getQuantity)
                    .orElse(0);
            default -> throw new IllegalArgumentException("Неверная операция: " + operation);
        };

        logger.info("Всего носков, соответствующих заданным критериям: {}", totalSocks);
        return totalSocks;
    }


    @Override
    public void updateSocks(Long id, SocksDto socksDto) {
        logger.info("Изменение параметров носков по id={}", id);
        Socks socks = socksRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Носков с id=%d не существует", id)));

        socks.setColor(socksDto.getColor());
        socks.setQuantity(socksDto.getQuantity());
        socks.setCottonPart(socksDto.getCottonPart());
        logger.info("Изменения внесены");
        socksRepository.save(socks);
    }

    @Override
    public void uploadBatch(MultipartFile file) {
        logger.info("Загрузка файла: {}", file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }
        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.endsWith(".csv")) {
            throw new IllegalArgumentException("Неверный формат файла. Требуется CSV.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Ошибки при обработке файлов.");

                }

                String color = parts[0].trim();
                int cottonPart = Integer.parseInt(parts[1].trim());
                int quantity = Integer.parseInt(parts[2].trim());

                SocksDto socksDto = new SocksDto();
                socksDto.setColor(color.toLowerCase());
                socksDto.setCottonPart(cottonPart);
                socksDto.setQuantity(quantity);

                registerIncome(socksDto);
            });
        } catch (Exception e) {
            throw new RuntimeException("Не удалось обработать файл", e);
        }
    }
}
