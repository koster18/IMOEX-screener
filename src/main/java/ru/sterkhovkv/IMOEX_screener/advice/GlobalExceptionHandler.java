package ru.sterkhovkv.IMOEX_screener.advice;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sterkhovkv.IMOEX_screener.dto.frontDTO.StockForm;
import ru.sterkhovkv.IMOEX_screener.exception.FormArgumentException;
import ru.sterkhovkv.IMOEX_screener.exception.TickerDataException;

import java.util.Objects;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(TickerDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleTickerDataException(TickerDataException ex) {
        log.error("TickerDataException occurred: {}; {}", ex.getMessage(), ex.toString());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(FormArgumentException.class)
    public String handleFormArgumentException(FormArgumentException ex, Model model,
                                              HttpSession session) {
        StockForm stockForm = (StockForm) session.getAttribute("stockForm");
        model.addAttribute("stockForm", Objects.requireNonNullElseGet(stockForm, StockForm::new));
        model.addAttribute("errorMessage", ex.getMessage());
        return "stocks";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(FormArgumentException ex, Model model,
                                         HttpSession session) {
        StockForm stockForm = (StockForm) session.getAttribute("stockForm");
        model.addAttribute("stockForm", Objects.requireNonNullElseGet(stockForm, StockForm::new));
        model.addAttribute("errorMessage", "Произошла ошибка: " + ex.getMessage());
        return "stocks";
    }
}
