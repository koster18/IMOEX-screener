package ru.sterkhovkv.IMOEX_screener.service;

import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.FormTickerDTO;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.StockForm;

import java.util.List;

public interface StockService {
    List<FormTickerDTO> loadStockTickersFromDB();
    void updateStockTickersDBIndexFromMoex();
    void updateStockTickersDBPricefromMoex();
    void updateStockTickersFromUser(List<FormTickerDTO> stockTickers);
    boolean addNewTicker(String newTicker);
    void clearBlankTickers();
    void refreshMoneyFromUser(int money);
    int getMoneyFromDB();
    double getCostinPortfolio();
}
