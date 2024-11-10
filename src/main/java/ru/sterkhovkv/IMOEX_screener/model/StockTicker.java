package ru.sterkhovkv.IMOEX_screener.model;

import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.Min;

@Entity
@Table(name = "stock_tickers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTicker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer Id;

    @Column(name = "ticker", nullable = false, length = 50)
    private String ticker;

    @Column(name = "shortname")
    private String shortname;

    @Column(name = "custom_weight")
    @Min(value = 0)
    private double customWeight = 1.00;

    @Column(name = "weight_imoex")
    private double weightImoex = 0;

    @Column(name = "weight_moex10")
    private double weightMoex10 = 0;

    @Column(name = "lotsize")
    private int lotsize = 1;

    @Column(name = "price")
    private double price;

    @Column(name = "count_in_portfolio")
    @Min(value = 0)
    private int countInPortfolio = 0;
}
