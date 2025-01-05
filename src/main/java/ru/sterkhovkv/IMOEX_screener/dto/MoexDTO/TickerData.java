package ru.sterkhovkv.IMOEX_screener.dto.MoexDTO;

public record TickerData(String ticker, String shortname, int lotsize, double price) {
}

