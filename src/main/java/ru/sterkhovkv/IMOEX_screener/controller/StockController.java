package ru.sterkhovkv.IMOEX_screener.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.StockForm;
import ru.sterkhovkv.IMOEX_screener.service.StockFormActionService;

import static ru.sterkhovkv.IMOEX_screener.dto.endpoints.Endpoints.STOCK_ENDPOINT;


@Controller
@SessionAttributes("stockForm")
@RequiredArgsConstructor
public class StockController {
    private final StockFormActionService stockFormActionService;

    @ModelAttribute("stockForm")
    public StockForm populateStockForm() {
        return stockFormActionService.createStockForm();
    }

    @GetMapping(STOCK_ENDPOINT)
    public String showStocks(Model model, @ModelAttribute("stockForm") StockForm stockForm) {
        stockFormActionService.updateStockForm(stockForm);
        model.addAttribute("stockForm", stockForm);
        return "stocks";
    }

    @PostMapping(STOCK_ENDPOINT)
    public String calculateWeights(@ModelAttribute("stockForm") StockForm stockForm,
                                   @RequestParam("action") String action,
                                   Model model) {
        stockFormActionService.handleAction(stockForm, action);
        stockFormActionService.updateStockForm(stockForm);
        model.addAttribute("stockForm", stockForm);
        return "stocks";
    }
}
