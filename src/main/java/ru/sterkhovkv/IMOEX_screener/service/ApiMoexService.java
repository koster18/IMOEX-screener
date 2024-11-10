package ru.sterkhovkv.IMOEX_screener.service;

import reactor.core.publisher.Mono;
import ru.sterkhovkv.IMOEX_screener.dto.StockTickerDTO;

import java.util.List;

public interface ApiMoexService {
    Mono<List<StockTickerDTO>> getAllStocksFromMoex();
    Mono<StockTickerDTO> fillTickerPrice(StockTickerDTO stockTickerDTO);
}
