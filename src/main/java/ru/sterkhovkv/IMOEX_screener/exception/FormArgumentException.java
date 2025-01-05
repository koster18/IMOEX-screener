package ru.sterkhovkv.IMOEX_screener.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FormArgumentException extends RuntimeException {
    public FormArgumentException(String message) {
        super(message);
    }
}
