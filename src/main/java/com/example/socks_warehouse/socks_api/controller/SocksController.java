package com.example.socks_warehouse.socks_api.controller;

import com.example.socks_warehouse.socks_api.dto.SocksDto;
import com.example.socks_warehouse.socks_api.service.SocksService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/socks")
public class SocksController {
    private final SocksService socksService;

    public SocksController(SocksService socksService) {
        this.socksService = socksService;
    }


    @Operation(summary = "Регистрация поступления носков")
    @PostMapping("/income")
    public ResponseEntity<Void> registerIncome(@RequestBody SocksDto socksDto) {
        socksService.registerIncome(socksDto);
        return ResponseEntity.ok().build();
    }



    @Operation(summary = "Регистрация отпуска носков")
    @PostMapping("/outcome")
    public ResponseEntity<Void> registerOutcome(@RequestBody SocksDto socksDto) {
        socksService.registerOutcome(socksDto);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Получить общее количество носков")
    @GetMapping
    public ResponseEntity<Integer> getTotalSocks(@RequestParam String color,
                                                 @RequestParam String operation,
                                                 @RequestParam int cottonPart) {
        int total = socksService.getTotalSocks(color, operation, cottonPart);
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Изменить данные носков")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateSocks(@PathVariable Long id, @RequestBody SocksDto socksDto) {
        socksService.updateSocks(id, socksDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Загрузить file.csv")
    @PostMapping("/batch")
    public ResponseEntity<String> uploadBatch(@RequestParam("file") MultipartFile file) {
        try {
            socksService.uploadBatch(file); // Логика обработки файла в сервисе
            return ResponseEntity.ok("Файл успешно загружен.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка обработки файла: " + e.getMessage());
        }
    }

}
