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
import ru.sterkhovkv.IMOEX_screener.exception.FormArgumentException;
import ru.sterkhovkv.IMOEX_screener.exception.TickerDataException;
import ru.sterkhovkv.IMOEX_screener.service.external.WebClientService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiMoexServiceImpl implements ApiMoexService {
    private final WebClientService webClientService;
    private final TickerDataProvider tickerDataProvider;

    @Override
    public List<StockTickerDTO> fetchAllIndexes() {
        List<ImoexIndexDTO> imoexIndexDTOList = webClientService.fetch(ExternalEndpoints.IMOEX_INDEX_URL,
                new ParameterizedTypeReference<List<ImoexIndexDTO>>() {
                }).block();
        log.debug("Получен Imoex индекс: {}", imoexIndexDTOList);

        if (imoexIndexDTOList == null || imoexIndexDTOList.isEmpty() || imoexIndexDTOList.size() < 2 ||
                imoexIndexDTOList.get(1).getAnalytics().isEmpty()) {
            throw new FormArgumentException("Не удалось загрузить Imoex индекс");
        }

        List<Moex10IndexDTO> moex10IndexDTOList = webClientService.fetch(ExternalEndpoints.INDEX10_URL,
                new ParameterizedTypeReference<List<Moex10IndexDTO>>() {
                }).block();
        log.debug("Получен Moex10 индекс: {}", moex10IndexDTOList);

        if (moex10IndexDTOList == null || moex10IndexDTOList.isEmpty() || moex10IndexDTOList.size() < 2 ||
                moex10IndexDTOList.get(1).getAnalytics().isEmpty()) {
            throw new TickerDataException("Не удалось загрузить Moex10 индекс");
        }

        return tickerDataProvider.combineTickersFromIndexes(
                imoexIndexDTOList.get(1).getAnalytics(),
                moex10IndexDTOList.get(1).getAnalytics());
    }

    @Override
    public List<StockTickerDTO> fetchAllTickers() {
        List<TickerMoexDTO> tickerMoexDTOList = webClientService.fetch(ExternalEndpoints.ALL_TICKERS_URL,
                new ParameterizedTypeReference<List<TickerMoexDTO>>() {
                }).block();
        log.debug("Получен список тикеров с ценами (при новом запросе): {}", tickerMoexDTOList);

        validateMoexData(tickerMoexDTOList);

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

        log.debug("Получен список тикеров с ценами (при обновлении): {}", tickerMoexDTOList);

        validateMoexData(tickerMoexDTOList);

        return tickerDataProvider.extractTickersFromImoexData(tickerMoexDTOList.get(1), stockTickers);
    }

    @Override
    public StockTickerDTO refreshSingleTickerPrice(StockTickerDTO stockTicker) {
        String tickerUrl = ExternalEndpoints.MOEX_API + ExternalEndpoints.TICKER_REQUEST + stockTicker.getTicker() + ExternalEndpoints.TICKER_REQUEST_PARAMS;
        List<TickerMoexDTO> tickerMoexDTOList = webClientService.fetch(tickerUrl,
                new ParameterizedTypeReference<List<TickerMoexDTO>>() {
                }).block();
        log.debug("Получены данные по тикеру {} : {}", stockTicker.getTicker(), tickerMoexDTOList);

        if (tickerMoexDTOList == null || tickerMoexDTOList.isEmpty() || tickerMoexDTOList.size() < 2 ||
                tickerMoexDTOList.get(1).getSecurities().isEmpty() || tickerMoexDTOList.get(1).getMarketdata().isEmpty()) {
            throw new FormArgumentException("Тикер " + stockTicker.getTicker() + " не найден");
        }
        return tickerDataProvider.extractSingleTickerFromImoexData(tickerMoexDTOList.get(1), stockTicker);
    }

    private void validateMoexData(List<TickerMoexDTO> tickerMoexDTOList) {
        if (tickerMoexDTOList == null || tickerMoexDTOList.isEmpty() || tickerMoexDTOList.size() < 2 ||
                tickerMoexDTOList.get(1).getSecurities().isEmpty() || tickerMoexDTOList.get(1).getMarketdata().isEmpty()) {
            throw new TickerDataException("Не удалось загрузить данные по тикерам");
        }
    }
}
