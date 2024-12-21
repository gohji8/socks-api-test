package com.example.socks_warehouse.socks_api.service;

import com.example.socks_warehouse.socks_api.dto.SocksDto;
import org.springframework.web.multipart.MultipartFile;


public interface SocksService {
    void registerIncome(SocksDto socksDto);
    void registerOutcome(SocksDto socksDto);
    int getTotalSocks(String color, String operation, int cottonPart);
    void updateSocks(Long id, SocksDto socksDto);
    void uploadBatch(MultipartFile file);
}
