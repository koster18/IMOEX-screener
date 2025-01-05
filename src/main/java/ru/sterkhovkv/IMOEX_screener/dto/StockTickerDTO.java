package ru.sterkhovkv.IMOEX_screener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockTickerDTO {
    private String ticker;
    private String shortname;
    private int lotsize;
    private double price;
    private double weightImoex = 0;
    private double weightMoex10 = 0;
}
