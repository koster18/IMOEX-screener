package ru.sterkhovkv.IMOEX_screener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.ImoexIndexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.Moex10IndexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.TickerMoexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.endpoints.ExternalEndpoints;
import ru.sterkhovkv.IMOEX_screener.dto.StockTickerDTO;
import ru.sterkhovkv.IMOEX_screener.service.external.WebClientService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiMoexServiceImpl implements ApiMoexService {
    private final WebClientService webClientService;
    private final TickerDataProvider tickerDataProvider;
    //TODO обработка ошибок, в том числе NullPointerException

    @Override
    public List<StockTickerDTO> fetchAllIndexes() {
        List<ImoexIndexDTO> imoexIndexDTOList = webClientService.fetch(ExternalEndpoints.IMOEX_INDEX_URL,
                new ParameterizedTypeReference<List<ImoexIndexDTO>>() {
                }).block();

        List<Moex10IndexDTO> moex10IndexDTOList = webClientService.fetch(ExternalEndpoints.INDEX10_URL,
                new ParameterizedTypeReference<List<Moex10IndexDTO>>() {
                }).block();

        return tickerDataProvider.combineTickersFromIndexes(
                imoexIndexDTOList.get(1).getAnalytics(),
                moex10IndexDTOList.get(1).getAnalytics());
    }

    @Override
    public List<StockTickerDTO> fetchAllTickers() {
        List<TickerMoexDTO> tickerMoexDTOList = webClientService.fetch(ExternalEndpoints.ALL_TICKERS_URL,
                new ParameterizedTypeReference<List<TickerMoexDTO>>() {
                }).block();

        return tickerDataProvider.extractTickersFromImoexData(tickerMoexDTOList.get(1), fetchAllIndexes());
    }

    @Override
    public List<StockTickerDTO> refreshTickerWeightsFromIndexes(List<StockTickerDTO> stockTickers) {
        List<StockTickerDTO> newStockTickers = fetchAllIndexes();

        stockTickers.forEach(stockTicker -> newStockTickers.stream()
                .filter(newStockTicker -> newStockTicker.getTicker().equals(stockTicker.getTicker()))
                .findFirst()
                .ifPresentOrElse(newStockTicker -> {
                    stockTicker.setWeightImoex(newStockTicker.getWeightImoex());
                    stockTicker.setWeightMoex10(newStockTicker.getWeightMoex10());
                }, () -> {
                    stockTicker.setWeightImoex(0.0);
                    stockTicker.setWeightMoex10(0.0);
                }));

        return stockTickers;
    }

    @Override
    public List<StockTickerDTO> refreshTickerPrices(List<StockTickerDTO> stockTickers) {
        List<TickerMoexDTO> tickerMoexDTOList = webClientService.fetch(ExternalEndpoints.ALL_TICKERS_URL,
                new ParameterizedTypeReference<List<TickerMoexDTO>>() {
                }).block();

        return tickerDataProvider.extractTickersFromImoexData(tickerMoexDTOList.get(1), stockTickers);
    }

    @Override
    public StockTickerDTO refreshSingleTickerPrice(StockTickerDTO stockTicker) {
        String tickerUrl = ExternalEndpoints.MOEX_API + ExternalEndpoints.TICKER_REQUEST + stockTicker.getTicker() + ExternalEndpoints.TICKER_REQUEST_PARAMS;
        List<TickerMoexDTO> tickerMoexDTOList = webClientService.fetch(tickerUrl,
                new ParameterizedTypeReference<List<TickerMoexDTO>>() {
                }).block();
        return tickerDataProvider.extractSingleTickerFromImoexData(tickerMoexDTOList.get(1), stockTicker);
    }
}
