package ru.sterkhovkv.IMOEX_screener.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.sterkhovkv.IMOEX_screener.dto.StockForm;
import ru.sterkhovkv.IMOEX_screener.service.ApiMoexService;
import ru.sterkhovkv.IMOEX_screener.service.StockService;


@Controller
public class StockController {
    private final StockService stockService;

    @Autowired
    public StockController(ApiMoexService apiMoexService, StockService stockService) {
        this.stockService = stockService;
    }

    @ModelAttribute("stockForm")
    public StockForm populateStockForm() {
        return createStockForm();
    }

    private StockForm createStockForm() {
        StockForm stockForm = new StockForm();
        stockForm.setMoney(stockService.getMoneyFromDB());
        stockForm.setCostInPortfolio(stockService.getCostinPortfolio());
        stockForm.setStocks(stockService.loadStockTickersFromDB());
        return stockForm;
    }

    @GetMapping("/stocks")
    public String showStocks(Model model, HttpSession session) {
        StockForm stockForm = (StockForm) session.getAttribute("stockForm");
        if (stockForm == null) {
            stockForm = createStockForm();
            session.setAttribute("stockForm", stockForm);
        } else {
            stockForm.setMoney(stockService.getMoneyFromDB());
            stockForm.setCostInPortfolio(stockService.getCostinPortfolio());
            stockForm.setStocks(stockService.loadStockTickersFromDB());
        }
        model.addAttribute("stockForm", stockForm);
        return "stocks";
    }

    @PostMapping("/stocks")
    public String calculateWeights(@ModelAttribute StockForm stockForm,
                                   @RequestParam("action") String action,
                                   Model model) {
        if ("save".equals(action)) {
            stockService.refreshMoneyFromUser(stockForm.getMoney());
            stockService.updateStockTickersFromUser(stockForm.getStocks());
        } else if ("update".equals(action)) {
            stockService.updateStockTickersDBIndexFromMoex();
        } else if ("refresh_prices".equals(action)) {
            stockService.updateStockTickersDBPricefromMoex();
        } else if ("add_ticker".equals(action)) {
            boolean isAdded = stockService.addNewTicker(stockForm.getNewTicker());
            if (!isAdded) {
                model.addAttribute("errorMessage", "Тикер не найден или не может быть добавлен.");
            }
        }
        stockForm.setCostInPortfolio(stockService.getCostinPortfolio());
        stockForm.setStocks(stockService.loadStockTickersFromDB());       ;
        model.addAttribute("stockForm", stockForm);
        return "stocks";
    }
}
