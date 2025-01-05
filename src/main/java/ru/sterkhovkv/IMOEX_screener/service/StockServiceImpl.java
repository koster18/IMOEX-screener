package ru.sterkhovkv.IMOEX_screener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.IndexData;
import ru.sterkhovkv.IMOEX_screener.dto.StockTickerDTO;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.FormTickerDTO;
import ru.sterkhovkv.IMOEX_screener.model.StockTicker;
import ru.sterkhovkv.IMOEX_screener.model.User;
import ru.sterkhovkv.IMOEX_screener.repository.StockTickerRepository;
import ru.sterkhovkv.IMOEX_screener.repository.UserRepository;
import ru.sterkhovkv.IMOEX_screener.util.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {
    private final StockTickerRepository stockTickerRepository;
    private final UserRepository userRepository;
    private final ApiMoexService apiMoexService;

    @Override
    public List<FormTickerDTO> loadStockTickersFromDB() {
        int money = userRepository.findByName(Constants.USERNAME).getMoney();
        return stockTickerRepository.findAll().stream()
                .map(stockTicker -> createFormDTO(stockTicker, money))
                .sorted((a, b) -> {
                    Double weightA = Optional.ofNullable(a.getIndexImoex()).map(IndexData::getWeight).orElse(Double.NEGATIVE_INFINITY);
                    Double weightB = Optional.ofNullable(b.getIndexImoex()).map(IndexData::getWeight).orElse(Double.NEGATIVE_INFINITY);
                    return Double.compare(weightB, weightA);
                })
                .collect(Collectors.toList());
    }


    private FormTickerDTO createFormDTO(StockTicker stockTicker, int money) {
        FormTickerDTO tickerFormDTO = new FormTickerDTO();
        tickerFormDTO.setTicker(stockTicker.getTicker());
        tickerFormDTO.setShortname(stockTicker.getShortname());
        tickerFormDTO.setLotsize(stockTicker.getLotsize());
        tickerFormDTO.setPrice(stockTicker.getPrice());

        tickerFormDTO.setCustomWeight(stockTicker.getCustomWeight());

        tickerFormDTO.setCountInPortfolio(stockTicker.getCountInPortfolio());
        BigDecimal roundedCost = BigDecimal.valueOf(stockTicker.getPrice() * stockTicker.getCountInPortfolio()).setScale(2, RoundingMode.HALF_UP);
        tickerFormDTO.setCostinPortfolio(roundedCost.doubleValue());
        tickerFormDTO.setStepCountInPortfolio(stockTicker.getLotsize());

        tickerFormDTO.setIndexImoex(createIndexData(money, stockTicker.getWeightImoex(), stockTicker.getCustomWeight(),
                stockTicker.getPrice(), stockTicker.getLotsize(), stockTicker.getCountInPortfolio()));
        tickerFormDTO.setIndexMoex10(createIndexData(money, stockTicker.getWeightMoex10(), stockTicker.getCustomWeight(),
                stockTicker.getPrice(), stockTicker.getLotsize(), stockTicker.getCountInPortfolio()));

        return tickerFormDTO;
    }

    private IndexData createIndexData (int money, double weight, double custom_weight, double price, int lotsize, int countInPortfolio) {
        IndexData indexData = new IndexData();
        indexData.setWeight(weight);

        int countToBuy = calculateCountToBuy(money, weight * custom_weight, price, lotsize);
        indexData.setCountToBuy(countToBuy);

        BigDecimal roundedCost = BigDecimal.valueOf(price * countToBuy * custom_weight).setScale(2, RoundingMode.HALF_UP);
        indexData.setPriceToBuy(roundedCost.doubleValue());

        int compliance = calculateCompliance(countInPortfolio, countToBuy);
        indexData.setCompliance(compliance);
        indexData.setComplianceColor(getComplianceColor(compliance));

        return indexData;
    }

    @Override
    @Transactional
    public void updateStockTickersDBIndexFromMoex() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        saveStockTickersToDb(apiMoexService.fetchAllTickers());
//        apiMoexService.getAllStocksFromMoex()
//                .subscribe(stockTickers -> saveStockTickersToDb(stockTickers),
//                        error -> log.error(error.getMessage()));
        log.info("Stock tickers from moex are updated in: {} ms", (System.currentTimeMillis() - timestamp.getTime()));
        clearBlankTickers();
    }

    @Override
    @Transactional
    public void updateStockTickersDBPricefromMoex() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        List<StockTickerDTO> tickersFromDB = stockTickerRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
        List<StockTicker> tickersFromMoex = apiMoexService.refreshTickerPrices(tickersFromDB).stream()
                .map(this::convertToEntity)
                .toList();
        stockTickerRepository.saveAll(tickersFromMoex);
        log.info("Stock ticker prices updated in: {} ms", (System.currentTimeMillis() - timestamp.getTime()));
        clearBlankTickers();
    }


    private void saveStockTickersToDb(List<StockTickerDTO> stockTickers) {
        List<StockTicker> stockTickersToUpdate = stockTickers.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        if (!stockTickersToUpdate.isEmpty()) {
            stockTickerRepository.saveAll(stockTickersToUpdate);
        }
    }

    private StockTicker convertToEntity(StockTickerDTO stockTickerDTO) {
        StockTicker stockTickerDB = stockTickerRepository.findFirstByTicker(stockTickerDTO.getTicker());
        if (stockTickerDB == null) {
            stockTickerDB = new StockTicker();
            stockTickerDB.setTicker(stockTickerDTO.getTicker());
        }
        stockTickerDB.setShortname(stockTickerDTO.getShortname());
        stockTickerDB.setLotsize(stockTickerDTO.getLotsize());
        stockTickerDB.setWeightImoex(stockTickerDTO.getWeightImoex());
        stockTickerDB.setWeightMoex10(stockTickerDTO.getWeightMoex10());
        stockTickerDB.setPrice(stockTickerDTO.getPrice());
        return stockTickerDB;
    }

    private StockTickerDTO convertToDTO(StockTicker stockTicker) {
        return StockTickerDTO.builder()
                .ticker(stockTicker.getTicker())
                .shortname(stockTicker.getShortname())
                .lotsize(stockTicker.getLotsize())
                .weightImoex(stockTicker.getWeightImoex())
                .weightMoex10(stockTicker.getWeightMoex10())
                .price(stockTicker.getPrice())
                .build();
    }

    @Override
    @Transactional
    public void updateStockTickersFromUser(List<FormTickerDTO> stockTickers) {
        List<StockTicker> stockTickersToUpdate = stockTickers.stream()
                .map(this::updateTickerCountInPortfolio)
                .filter(ticker -> ticker != null)
                .collect(Collectors.toList());

        if (!stockTickersToUpdate.isEmpty()) {
            stockTickerRepository.saveAll(stockTickersToUpdate);
        }
    }

    private StockTicker updateTickerCountInPortfolio(FormTickerDTO stockTicker) {
        StockTicker stockTickerDB = stockTickerRepository.findFirstByTicker(stockTicker.getTicker());
        if (stockTickerDB != null) {
            stockTickerDB.setCountInPortfolio(stockTicker.getCountInPortfolio());
            stockTickerDB.setCustomWeight(stockTicker.getCustomWeight());
            return stockTickerDB;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean addNewTicker(String newTicker) {
        if (newTicker == null || newTicker.isEmpty()) {
            return false;
        }
        StockTickerDTO stockTickerDTO = new StockTickerDTO();
        StockTicker stockTickerDB = stockTickerRepository.findFirstByTicker(newTicker);
        if (stockTickerDB != null) {
            stockTickerDTO = convertToDTO(stockTickerDB);
        } else {
            stockTickerDTO.setTicker(newTicker);
            stockTickerDTO.setLotsize(1);
            stockTickerDTO.setPrice(0);
        }
        stockTickerDTO = apiMoexService.refreshSingleTickerPrice(stockTickerDTO);
        //apiMoexService.fillTickerPrice(stockTickerDTO).subscribe();
        if (stockTickerDTO.getShortname() == null || stockTickerDTO.getShortname().isEmpty()) {
            return false;
        } else {
            stockTickerRepository.save(convertToEntity(stockTickerDTO));
            return true;
        }
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

    private int calculateCountToBuy(int money, double weight, double price, int lotsize) {
        if ((price == 0) | lotsize == 0) {
            return 0;
        } else {
            return (int) Math.round((money * weight) / (100 * price * lotsize)) * lotsize;
        }
    }

    private int calculateCompliance(int countInPortfolio, int countToBuy) {
        if (countToBuy > 0) {
            return (int) Math.round((double) countInPortfolio / countToBuy * 100);
        } else {
            return 0;
        }
    }

    private String getComplianceColor(double compliance) {
        if (compliance <= 0) {
            return "yellow";
        } else if (compliance <= 100) {
            int red = 255 - (int) Math.round((compliance / 100) * 255);
            int green = (int) Math.round((compliance / 100) * 255);
            return "rgb(" + red + "," + green + ",0)";
        } else if (compliance <= 200) {
            int red = (int) Math.round(((compliance - 100) / 100.0) * 255);
            int green = 255 - red;
            return "rgb(" + red + "," + green + ",0)";
        } else {
            return "red";
        }
    }

}
