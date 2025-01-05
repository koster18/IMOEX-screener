package ru.sterkhovkv.IMOEX_screener.dto;

public record TickerData(String ticker, String shortname, int lotsize, double price) {
}

