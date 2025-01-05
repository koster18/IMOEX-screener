package ru.sterkhovkv.IMOEX_screener.dto.frontDTO;

import lombok.Data;

import java.util.List;

@Data
public class StockForm {
    private int money;
    private double costInPortfolio;
    private List<FormTickerDTO> stocks;
    private String newTicker;
}
