package ru.sterkhovkv.IMOEX_screener.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.sterkhovkv.IMOEX_screener.dto.*;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.ImoexIndexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.Moex10IndexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.MoexDTO.TickerMoexDTO;
import ru.sterkhovkv.IMOEX_screener.dto.endpoints.Endpoints;
import ru.sterkhovkv.IMOEX_screener.repository.StockTickerRepository;

import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ApiMoexServiceImpl implements ApiMoexService {
    private final ObjectMapper objectMapper;

    @Autowired
    public ApiMoexServiceImpl(ObjectMapper objectMapper, StockTickerRepository stockTickerRepository) {
        this.objectMapper = objectMapper;
    }

    public Mono<List<StockTickerDTO>> getAllStocksFromMoex() {
        return getStockList()
                .collectList()
                .flatMap(this::updateMoex10Weight)
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::fillTickerPrice)
                .collectList();
    }

    @SneakyThrows
    public Mono<StockTickerDTO> fillTickerPrice(@NonNull StockTickerDTO stockTickerDTO) {
        String tickerUrl = Endpoints.MOEX_API + Endpoints.TIKER_REQUEST + stockTickerDTO.getTicker() + Endpoints.TIKER_REQUEST_PARAMS;
        return Mono.fromCallable(() -> objectMapper.readValue(new URL(tickerUrl), new TypeReference<List<TickerMoexDTO>>() {}))
                .flatMap(tickerMoexDTO -> {
                    if (tickerMoexDTO != null && tickerMoexDTO.size() > 1) {

                        if (tickerMoexDTO.get(1).getSecurities() != null && !tickerMoexDTO.get(1).getSecurities().isEmpty()) {
                            TickerMoexDTO.Securities securities = tickerMoexDTO.get(1).getSecurities().get(0);
                            stockTickerDTO.setShortname(securities.getShortnames());
                            stockTickerDTO.setLotsize(securities.getLotsize());
                        }
                        if (tickerMoexDTO.get(1).getMarketdata() != null && !tickerMoexDTO.get(1).getMarketdata().isEmpty()) {
                            TickerMoexDTO.Marketdata marketdata = tickerMoexDTO.get(1).getMarketdata().get(0);
                            Double price = Optional.of(marketdata.getPrice())
                                    .filter(p -> p != 0)
                                    .orElseGet(() -> Optional.of(marketdata.getPrice_last())
                                            .filter(lastPrice -> lastPrice != 0)
                                            .orElse(0.0));
                            stockTickerDTO.setPrice(price);
                        }
                    }
                    return Mono.just(stockTickerDTO);
                })
                .doOnError(e -> log.error("Error fetching ticker price for {}: {}", stockTickerDTO.getTicker(), e.getMessage()));
    }

    @SneakyThrows
    public Flux<StockTickerDTO> getStockList() {
        String imoexIndexUrl = Endpoints.MOEX_API + Endpoints.INDEX_REQUEST + Endpoints.INDEX_REQUEST_PARAMS;
        return Mono.fromCallable(() -> objectMapper.readValue(new URL(imoexIndexUrl), new TypeReference<List<ImoexIndexDTO>>() {}))
                .flatMapMany(imoexIndexDTOList -> {
                    if (imoexIndexDTOList != null && imoexIndexDTOList.size() > 1) {
                        return Flux.fromIterable(imoexIndexDTOList.get(1).getAnalytics())
                                .map(analytics -> {
                                    StockTickerDTO stockTickerDTO = new StockTickerDTO();
                                    stockTickerDTO.setShortname(analytics.getShortnames());
                                    stockTickerDTO.setTicker(analytics.getTicker());
                                    stockTickerDTO.setWeightImoex(analytics.getWeight());
                                    return stockTickerDTO;
                                });
                    }
                    return Flux.empty();
                })
                .doOnError(e -> log.error("Error fetching stock list: {}", e.getMessage()));
    }

    @SneakyThrows
    public Mono<List<StockTickerDTO>> updateMoex10Weight(List<StockTickerDTO> stockTickerDTOList) {
        String index10Url = Endpoints.MOEX_API + Endpoints.INDEX10_REQUEST + Endpoints.INDEX10_REQUEST_PARAMS;
        return Mono.fromCallable(() -> objectMapper.readValue(new URL(index10Url), new TypeReference<List<Moex10IndexDTO>>() {}))
                .flatMap(moex10IndexDTOList -> {
                    if (moex10IndexDTOList != null && moex10IndexDTOList.size() > 1) {
                        List<Moex10IndexDTO.Analytics> analyticsList = moex10IndexDTOList.get(1).getAnalytics();
                        analyticsList.forEach(analytics -> {
                            stockTickerDTOList.stream()
                                    .filter(stock -> stock.getTicker().equals(analytics.getTicker()))
                                    .findFirst()
                                    .ifPresent(stock -> stock.setWeightMoex10(analytics.getWeight()));
                        });
                    }
                    return Mono.just(stockTickerDTOList); // Возвращаем обновленный список
                })
                .doOnError(e -> log.error("Error fetching Moex10 weights: {}", e.getMessage()));
    }
}
