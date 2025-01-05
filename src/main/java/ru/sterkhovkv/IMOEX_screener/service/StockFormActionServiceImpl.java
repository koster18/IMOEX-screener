package ru.sterkhovkv.IMOEX_screener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.FormTickerDTO;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.IndexData;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.StockForm;
import ru.sterkhovkv.IMOEX_screener.exception.FormArgumentException;
import ru.sterkhovkv.IMOEX_screener.model.StockTicker;
import ru.sterkhovkv.IMOEX_screener.repository.StockTickerRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockFormActionServiceImpl implements StockFormActionService {
    private final StockTickerRepository stockTickerRepository;
    private final StockService stockService;

    @Override
    public void handleAction(StockForm stockForm, String action) throws FormArgumentException {
        switch (action) {
            case "save":
                log.debug("Получен запрос на сохранение данных");
                stockService.refreshMoneyFromUser(stockForm.getMoney());
                stockService.updateStockTickersFromUser(stockForm.getStocks());
                break;
            case "update":
                log.debug("Получен запрос на обновление индексов");
                stockService.updateStockTickersDBIndexFromMoex();
                break;
            case "refresh_prices":
                log.debug("Получен запрос на обновление цен");
                stockService.updateStockTickersDBPricefromMoex();
                break;
            case "add_ticker":
                log.debug("Получен запрос на добваление тикера");
                stockService.addNewTicker(stockForm.getNewTicker());
                break;
            default:
                throw new FormArgumentException("Неизвестное действие: " + action);
        }
    }

    @Override
    public StockForm createStockForm() {
        StockForm stockForm = new StockForm();
        updateStockForm(stockForm);
        return stockForm;
    }

    @Override
    public void updateStockForm(StockForm stockForm) {
        stockForm.setMoney(stockService.getMoneyFromDB());
        stockForm.setCostInPortfolio(stockService.getCostinPortfolio());
        stockForm.setStocks(loadStockTickersFromDB());
    }

    @Override
    public List<FormTickerDTO> loadStockTickersFromDB() {
        int money = stockService.getMoneyFromDB();
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
        BigDecimal roundedCost = BigDecimal.valueOf(stockTicker.getPrice() * stockTicker.getCountInPortfolio()).setScale(2, RoundingMode.HALF_UP);
        IndexData indexDataImoex = createIndexData(money, stockTicker.getWeightImoex(), stockTicker.getCustomWeight(),
                stockTicker.getPrice(), stockTicker.getLotsize(), stockTicker.getCountInPortfolio());
        IndexData indexDataMoex10 = createIndexData(money, stockTicker.getWeightMoex10(), stockTicker.getCustomWeight(),
                stockTicker.getPrice(), stockTicker.getLotsize(), stockTicker.getCountInPortfolio());

        return FormTickerDTO.builder()
                .ticker(stockTicker.getTicker())
                .shortname(stockTicker.getShortname())
                .lotsize(stockTicker.getLotsize())
                .price(stockTicker.getPrice())
                .customWeight(stockTicker.getCustomWeight())
                .countInPortfolio(stockTicker.getCountInPortfolio())
                .costinPortfolio(roundedCost.doubleValue())
                .stepCountInPortfolio(stockTicker.getLotsize())
                .indexImoex(indexDataImoex)
                .indexMoex10(indexDataMoex10)
                .build();
    }


    private static IndexData createIndexData(int money, double weight, double custom_weight, double price, int lotsize, int countInPortfolio) {
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

    private static int calculateCountToBuy(int money, double weight, double price, int lotsize) {
        if ((price == 0) | lotsize == 0) {
            return 0;
        } else {
            return (int) Math.round((money * weight) / (100 * price * lotsize)) * lotsize;
        }
    }

    private static int calculateCompliance(int countInPortfolio, int countToBuy) {
        if (countToBuy > 0) {
            return (int) Math.round((double) countInPortfolio / countToBuy * 100);
        } else {
            return 0;
        }
    }

    private static String getComplianceColor(double compliance) {
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
