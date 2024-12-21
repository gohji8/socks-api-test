package com.example.socks_warehouse.socks_api;

import com.example.socks_warehouse.socks_api.dto.SocksDto;
import com.example.socks_warehouse.socks_api.model.Socks;
import com.example.socks_warehouse.socks_api.repository.SocksRepository;
import com.example.socks_warehouse.socks_api.service.impl.SocksServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class SocksServiceImplTest {

    @Mock
    private SocksRepository socksRepository;

    private SocksServiceImpl socksService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        socksService = new SocksServiceImpl(socksRepository);
    }

    @Test
    void testRegisterOutcomeSocksNotFound() {
        SocksDto socksDto = new SocksDto("red", 50, 100);

        when(socksRepository.findByColorAndCottonPart(socksDto.getColor(), socksDto.getCottonPart()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> socksService.registerOutcome(socksDto));
        assertTrue(exception.getMessage().contains("Носки такого цвета и содержания хлопка не найдены"));
    }

    @Test
    void testGetTotalSocksInvalidOperation() {
        SocksDto socksDto = new SocksDto("red", 50, 100);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> socksService.getTotalSocks("red", "invalid", 50));
        assertTrue(exception.getMessage().contains("Неверная операция"));
    }

    @Test
    void testUpdateSocks_Success() {
        Long id = 1L;
        Socks existingSocks = new Socks();
        existingSocks.setId(id);
        existingSocks.setColor("red");
        existingSocks.setCottonPart(50);
        existingSocks.setQuantity(100);

        SocksDto socksDto = new SocksDto();
        socksDto.setColor("blue");
        socksDto.setCottonPart(70);
        socksDto.setQuantity(150);

        when(socksRepository.findById(id)).thenReturn(Optional.of(existingSocks));

        socksService.updateSocks(id, socksDto);

        verify(socksRepository).findById(id);
        verify(socksRepository).save(argThat(socks ->
                socks.getColor().equals("blue") &&
                        socks.getCottonPart() == 70 &&
                        socks.getQuantity() == 150
        ));
    }

    @Test
    void testUpdateSocks_NotFound() {
        Long id = 2L;
        SocksDto socksDto = new SocksDto();
        socksDto.setColor("blue");
        socksDto.setCottonPart(70);
        socksDto.setQuantity(150);

        when(socksRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> socksService.updateSocks(id, socksDto));

        verify(socksRepository, never()).save(any());
    }

    @Test
    void testUploadBatch() throws Exception {
        String content = "red,50,100\nblue,40,150";
        MockMultipartFile file = new MockMultipartFile("file", "batch.csv", "text/csv", content.getBytes());

        socksService.uploadBatch(file);

        verify(socksRepository, times(2)).save(any(Socks.class)); // Проверяем, что два раза сохраняются носки
    }
}
