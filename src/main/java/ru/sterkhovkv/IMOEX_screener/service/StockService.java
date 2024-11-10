package ru.sterkhovkv.IMOEX_screener.service;

import ru.sterkhovkv.IMOEX_screener.dto.StockTickerFormDTO;

import java.util.List;

public interface StockService {
    List<StockTickerFormDTO> loadStockTickersFromDB();
    void updateStockTickersDBIndexFromMoex();
    void updateStockTickersDBPricefromMoex();
    void updateStockTickersFromUser(List<StockTickerFormDTO> stockTickers);
    boolean addNewTicker(String newTicker);
    void clearBlankTickers();
    void refreshMoneyFromUser(int money);
    int getMoneyFromDB();
    double getCostinPortfolio();
}
