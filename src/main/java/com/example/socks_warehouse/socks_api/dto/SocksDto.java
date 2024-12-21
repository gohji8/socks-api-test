package com.example.socks_warehouse.socks_api.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SocksDto {
    private String color;
    private int cottonPart;
    private int quantity;

    public SocksDto() {}

    public SocksDto(String red, int i, int i1) {}
}
