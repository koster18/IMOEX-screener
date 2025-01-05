package ru.sterkhovkv.IMOEX_screener.dto.frontDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormTickerDTO {
    private String ticker;
    private String shortname;
    private int lotsize;
    private double price;

    private int countInPortfolio = 0;
    private double costinPortfolio = 0;
    private int stepCountInPortfolio = 1;

    private double customWeight = 1.00;

    private IndexData indexImoex;
    private IndexData indexMoex10;
}
