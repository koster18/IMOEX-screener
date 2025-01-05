package ru.sterkhovkv.IMOEX_screener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sterkhovkv.IMOEX_screener.dto.StockTickerDTO;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.FormTickerDTO;
import ru.sterkhovkv.IMOEX_screener.exception.FormArgumentException;
import ru.sterkhovkv.IMOEX_screener.mapper.StockTickerMapper;
import ru.sterkhovkv.IMOEX_screener.model.StockTicker;
import ru.sterkhovkv.IMOEX_screener.model.User;
import ru.sterkhovkv.IMOEX_screener.repository.StockTickerRepository;
import ru.sterkhovkv.IMOEX_screener.repository.UserRepository;
import ru.sterkhovkv.IMOEX_screener.util.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {
    private final StockTickerRepository stockTickerRepository;
    private final UserRepository userRepository;
    private final ApiMoexService apiMoexService;
    private final StockTickerMapper stockTickerMapper;

    @Override
    @Transactional
    public void updateStockTickersDBIndexFromMoex() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        saveStockTickersToDb(apiMoexService.fetchAllTickers());
        log.info("Индексы обновлены за: {} мс", (System.currentTimeMillis() - timestamp.getTime()));
        clearBlankTickers();
    }

    @Override
    @Transactional
    public void updateStockTickersDBPricefromMoex() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        List<StockTicker> tickersFromDB = stockTickerRepository.findAll();

        List<StockTickerDTO> tickersFromMoex = apiMoexService.refreshTickerPrices(
                tickersFromDB.stream()
                        .map(stockTickerMapper::toDTO)
                        .toList()
        );

        List<StockTicker> stockTickersToUpdate = updateOrCreateTickers(tickersFromMoex, tickersFromDB);

        stockTickerRepository.saveAll(stockTickersToUpdate);

        log.info("Цены акций обновлены за: {} мс", (System.currentTimeMillis() - timestamp.getTime()));
        clearBlankTickers();
    }

    private List<StockTicker> updateOrCreateTickers(List<StockTickerDTO> stockTickerDTOs, List<StockTicker> existingTickers) {
        Map<String, StockTicker> tickerMap = existingTickers.stream()
                .collect(Collectors.toMap(StockTicker::getTicker, stockTicker -> stockTicker));

        List<StockTicker> stockTickersToUpdate = new ArrayList<>();
        for (StockTickerDTO stockTickerDTO : stockTickerDTOs) {
            StockTicker stockTickerDB = tickerMap.get(stockTickerDTO.getTicker());
            if (stockTickerDB == null) {
                stockTickerDB = stockTickerMapper.toEntity(stockTickerDTO);
            } else {
                stockTickerMapper.updateEntity(stockTickerDB, stockTickerDTO);
            }
            stockTickersToUpdate.add(stockTickerDB);
        }
        return stockTickersToUpdate;
    }


    private void saveStockTickersToDb(List<StockTickerDTO> stockTickers) {
        List<StockTicker> existingTickers = stockTickerRepository.findAll();

        List<StockTicker> stockTickersToUpdate = updateOrCreateTickers(stockTickers, existingTickers);

        if (!stockTickersToUpdate.isEmpty()) {
            stockTickerRepository.saveAll(stockTickersToUpdate);
        }
    }

    @Override
    @Transactional
    public void updateStockTickersFromUser(List<FormTickerDTO> stockTickers) {
        List<String> tickers = stockTickers.stream()
                .map(FormTickerDTO::getTicker)
                .collect(Collectors.toList());

        List<StockTicker> stockTickersDB = stockTickerRepository.findByTickers(tickers);

        Map<String, StockTicker> stockTickerMap = stockTickersDB.stream()
                .collect(Collectors.toMap(StockTicker::getTicker, ticker -> ticker));

        List<StockTicker> stockTickersToUpdate = stockTickers.stream()
                .map(stockTickerDTO -> updateTickerCountInPortfolio(stockTickerDTO, stockTickerMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!stockTickersToUpdate.isEmpty()) {
            stockTickerRepository.saveAll(stockTickersToUpdate);
        }
    }

    private StockTicker updateTickerCountInPortfolio(FormTickerDTO stockTickerDTO,
                                                     Map<String, StockTicker> stockTickerMap) {
        StockTicker stockTickerDB = stockTickerMap.get(stockTickerDTO.getTicker());
        if (stockTickerDB != null) {
            stockTickerDB.setCountInPortfolio(stockTickerDTO.getCountInPortfolio());
            stockTickerDB.setCustomWeight(stockTickerDTO.getCustomWeight());
            return stockTickerDB;
        }
        return null;
    }


    @Override
    @Transactional
    public void addNewTicker(String newTicker) {
        if (newTicker == null || newTicker.isEmpty()) {
            throw new FormArgumentException("Тикер не может быть пустым");
        }
        StockTicker stockTickerDB = stockTickerRepository.findFirstByTicker(newTicker);
        if (stockTickerDB != null) {
            throw new FormArgumentException("Тикер уже добавлен");
        }
        StockTickerDTO stockTickerDTO = StockTickerDTO.builder()
                .ticker(newTicker)
                .weightMoex10(0.0)
                .weightImoex(0.0)
                .lotsize(1)
                .price(0)
                .build();
        stockTickerDTO = apiMoexService.refreshSingleTickerPrice(stockTickerDTO);
        stockTickerRepository.save(stockTickerMapper.toEntity(stockTickerDTO));
    }

    @Override
    public void clearBlankTickers() {
        stockTickerRepository.deleteAll(
                stockTickerRepository.findByCountInPortfolioAndWeightImoexAndWeightMoex10(0, 0, 0));
    }

    @Override
    @Transactional
    public void refreshMoneyFromUser(int money) {
        User user = userRepository.findByName(Constants.USERNAME);
        user.setMoney(money);
        userRepository.save(user);
    }


    @Override
    public int getMoneyFromDB() {
        return userRepository.findByName(Constants.USERNAME).getMoney();
    }

    @Override
    public double getCostinPortfolio() {
        double totalCost = stockTickerRepository.findAll().stream()
                .mapToDouble(stockTicker -> stockTicker.getCountInPortfolio() * stockTicker.getPrice())
                .sum();

        BigDecimal roundedCost = BigDecimal.valueOf(totalCost).setScale(2, RoundingMode.HALF_UP);

        return roundedCost.doubleValue();
    }
}
