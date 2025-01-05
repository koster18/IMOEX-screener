package ru.sterkhovkv.IMOEX_screener.service;

import org.springframework.transaction.annotation.Transactional;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.FormTickerDTO;

import java.util.List;

public interface StockService {

    void updateStockTickersDBIndexFromMoex();

    void updateStockTickersDBPricefromMoex();

    void updateStockTickersFromUser(List<FormTickerDTO> stockTickers);

    void addNewTicker(String newTicker);

    void clearBlankTickers();

    @Transactional
    void refreshMoneyFromUser(int money);

    int getMoneyFromDB();

    double getCostinPortfolio();
}
