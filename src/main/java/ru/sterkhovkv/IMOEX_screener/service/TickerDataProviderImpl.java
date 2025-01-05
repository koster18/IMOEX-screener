package ru.sterkhovkv.IMOEX_screener.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.ImoexIndexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.Moex10IndexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.TickerData;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.TickerMoexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.StockTickerDTO;
import ru.sterkhovkv.IMOEX_screener.exception.TickerDataException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TickerDataProviderImpl implements TickerDataProvider {

    @Override
    public List<StockTickerDTO> extractTickersFromImoexIndex(List<ImoexIndexDTO.Analytics> analyticsImoexList) {
        return analyticsImoexList.stream()
                .map(this::mapToStockTickerDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockTickerDTO> combineTickersFromIndexes(List<ImoexIndexDTO.Analytics> analyticsImoexList,
                                                          List<Moex10IndexDTO.Analytics> analyticsMoex10List) {
        Map<String, Double> analyticsMoex10Map = analyticsMoex10List.stream()
                .collect(Collectors.toMap(Moex10IndexDTO.Analytics::getTicker, Moex10IndexDTO.Analytics::getWeight));

        return analyticsImoexList.stream()
                .map(analytics -> mapToStockTickerDTO(analytics, analyticsMoex10Map.get(analytics.getTicker())))
                .collect(Collectors.toList());
    }

    @Override
    public StockTickerDTO extractSingleTickerFromImoexData(TickerMoexDTO tickerMoexList, StockTickerDTO stockTicker) {
        Map<String, TickerData> tickerDataMap = getTickerMap(tickerMoexList);
        return enrichStockTickerDTO(stockTicker, tickerDataMap.get(stockTicker.getTicker()));
    }

    @Override
    public List<StockTickerDTO> extractTickersFromImoexData(TickerMoexDTO tickerMoexList, List<StockTickerDTO> stockTickerList) {
        Map<String, TickerData> tickerDataMap = getTickerMap(tickerMoexList);

        return stockTickerList.stream()
                .map(stockTicker -> enrichStockTickerDTO(stockTicker, tickerDataMap.get(stockTicker.getTicker())))
                .collect(Collectors.toList());
    }

    private StockTickerDTO enrichStockTickerDTO(StockTickerDTO stockTicker, TickerData tickerData) {
        StockTickerDTO.StockTickerDTOBuilder builder = StockTickerDTO.builder()
                .ticker(stockTicker.getTicker())
                .shortname(stockTicker.getShortname())
                .weightImoex(stockTicker.getWeightImoex())
                .weightMoex10(stockTicker.getWeightMoex10());

        if (tickerData != null) {
            builder.lotsize(tickerData.lotsize())
                    .shortname(tickerData.shortname())
                    .price(tickerData.price());
        }

        return builder.build();
    }

    private StockTickerDTO mapToStockTickerDTO(ImoexIndexDTO.Analytics analytics) {
        return StockTickerDTO.builder()
                .ticker(analytics.getTicker())
                .shortname(analytics.getShortnames())
                .weightImoex(analytics.getWeight())
                .build();
    }

    private StockTickerDTO mapToStockTickerDTO(ImoexIndexDTO.Analytics analytics, Double weightMoex10) {
        return StockTickerDTO.builder()
                .ticker(analytics.getTicker())
                .shortname(analytics.getShortnames())
                .weightImoex(analytics.getWeight())
                .weightMoex10(weightMoex10 != null ? weightMoex10 : 0.0)
                .build();
    }

    private static Map<String, TickerData> getTickerMap(TickerMoexDTO tickerMoexDTO) {
        if (tickerMoexDTO.getMarketdata().size() != tickerMoexDTO.getSecurities().size()) {
            throw new TickerDataException("Invalid ticker data: Marketdata and Securities size are not equal");
        }

        return IntStream.range(0, tickerMoexDTO.getSecurities().size())
                .mapToObj(i -> createTickerData(tickerMoexDTO, i))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TickerData::ticker, Function.identity()));
    }

    private static TickerData createTickerData(TickerMoexDTO tickerMoexDTO, int index) {
        TickerMoexDTO.Securities securities = tickerMoexDTO.getSecurities().get(index);
        if (securities == null) return null;

        String ticker = securities.getTicker();
        String shortname = securities.getShortnames();
        int lotsize = securities.getLotsize();

        double price = Optional.ofNullable(tickerMoexDTO.getMarketdata().get(index))
                .map(marketdata -> Optional.of(marketdata.getPrice())
                        .filter(p -> p != 0)
                        .orElse(Optional.of(marketdata.getPrice_last())
                                .filter(lastPrice -> lastPrice != 0)
                                .orElse(0.0)))
                .orElse(0.0);

        return new TickerData(ticker, shortname, lotsize, price);
    }
}
