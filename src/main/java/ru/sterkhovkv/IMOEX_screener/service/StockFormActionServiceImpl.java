package ru.sterkhovkv.IMOEX_screener.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.StockForm;
import ru.sterkhovkv.IMOEX_screener.exception.FormArgumentException;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockFormActionServiceImpl implements StockFormActionService {
    private final StockService stockService;

    @Override
    public void handleAction(StockForm stockForm, String action) throws FormArgumentException {
        switch (action) {
            case "save":
                stockService.refreshMoneyFromUser(stockForm.getMoney());
                stockService.updateStockTickersFromUser(stockForm.getStocks());
                break;
            case "update":
                stockService.updateStockTickersDBIndexFromMoex();
                break;
            case "refresh_prices":
                stockService.updateStockTickersDBPricefromMoex();
                break;
            case "add_ticker":
                boolean isAdded = stockService.addNewTicker(stockForm.getNewTicker());
                if (!isAdded) {
                    throw new FormArgumentException("Тикер не найден или не может быть добавлен.");
                }
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
        stockForm.setStocks(stockService.loadStockTickersFromDB());
    }
}
