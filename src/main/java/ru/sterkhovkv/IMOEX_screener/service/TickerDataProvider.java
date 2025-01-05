package ru.sterkhovkv.IMOEX_screener.service;

import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.ImoexIndexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.Moex10IndexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.TickerMoexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.StockTickerDTO;

import java.util.List;

public interface TickerDataProvider {
    List<StockTickerDTO> extractTickersFromImoexIndex(List<ImoexIndexDTO.Analytics> analyticsImoexList);

    List<StockTickerDTO> combineTickersFromIndexes(List<ImoexIndexDTO.Analytics> analyticsImoexList,
                                                   List<Moex10IndexDTO.Analytics> analyticsMoex10List);

    StockTickerDTO extractSingleTickerFromImoexData(TickerMoexDTO tickerMoexList,
                                                    StockTickerDTO stockTicker);

    List<StockTickerDTO> extractTickersFromImoexData(TickerMoexDTO tickerMoexList,
                                                     List<StockTickerDTO> stockTickerList);
}
