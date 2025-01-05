package ru.sterkhovkv.IMOEX_screener.service;

import ru.sterkhovkv.IMOEX_screener.dto.StockTickerDTO;

import java.util.List;

public interface ApiMoexService {
    List<StockTickerDTO> fetchAllIndexes();

    List<StockTickerDTO> fetchAllTickers();

    List<StockTickerDTO> refreshTickerWeightsFromIndexes(List<StockTickerDTO> stockTickers);

    List<StockTickerDTO> refreshTickerPrices(List<StockTickerDTO> stockTickers);

    StockTickerDTO refreshSingleTickerPrice(StockTickerDTO stockTicker);
}
