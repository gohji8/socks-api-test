package com.example.socks_warehouse.socks_api.repository;

import com.example.socks_warehouse.socks_api.model.Socks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocksRepository extends JpaRepository<Socks, Long> {
    Optional<Socks> findByColorAndCottonPart(String color, int cottonPart);
    List<Socks> findByColorAndCottonPartGreaterThan(String color, int cottonPart);

    List<Socks> findByColorAndCottonPartLessThan(String color, int cottonPart);
}
