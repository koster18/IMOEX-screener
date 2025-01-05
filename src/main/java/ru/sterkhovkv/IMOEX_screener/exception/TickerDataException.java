package ru.sterkhovkv.IMOEX_screener.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TickerDataException extends RuntimeException {

    public TickerDataException(String message) {
        super(message);
    }

    public TickerDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
