package ru.sterkhovkv.IMOEX_screener.service;

import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.StockForm;
import ru.sterkhovkv.IMOEX_screener.exception.FormArgumentException;

public interface StockFormActionService {

    void handleAction(StockForm stockForm, String action) throws FormArgumentException;

    StockForm createStockForm();

    void updateStockForm(StockForm stockForm);
}
