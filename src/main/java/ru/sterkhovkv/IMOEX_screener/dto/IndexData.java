package ru.sterkhovkv.IMOEX_screener.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexData {
    private double weight = 0;
    private int countToBuy = 0;
    private double priceToBuy = 0;
    private int compliance = 0;
    private String complianceColor = "";
}
