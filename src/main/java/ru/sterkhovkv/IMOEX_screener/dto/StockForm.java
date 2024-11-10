package ru.sterkhovkv.IMOEX_screener.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockForm {
    private int money;
    private double costInPortfolio;
    private List<StockTickerFormDTO> stocks;
    private String newTicker;
}
