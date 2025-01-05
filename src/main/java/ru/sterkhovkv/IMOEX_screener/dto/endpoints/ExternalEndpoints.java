package ru.sterkhovkv.IMOEX_screener.dto.endpoints;

//Full get request for ticker
//https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities/SBER.json?iss.meta=off&marketdata.columns=LAST,MARKETPRICE&iss.version=on&iss.json=extended&securities.columns=SECID,SHORTNAME,LOTSIZE&dataversion.columns=data_version
//
//Full get request for all tickers
//https://iss.moex.com/iss/engines/stock/markets/shares/boards/TQBR/securities/.json?iss.meta=off&marketdata.columns=LAST,MARKETPRICE&iss.version=on&iss.json=extended&securities.columns=SECID,SHORTNAME,LOTSIZE&dataversion.columns=data_version
//
//Full get request for index
//https://iss.moex.com/iss/statistics/engines/stock/markets/index/analytics/IMOEX.json?iss.meta=off&analytics.columns=ticker,shortnames,weight&iss.version=off&iss.json=extended&analytics.cursor.columns=TOTAL&analytics.dates.columns=till&limit=100
//
//Full get request for index10
//https://iss.moex.com/iss/statistics/engines/stock/markets/index/analytics/MOEX10.json?iss.meta=off&analytics.columns=ticker,shortnames,weight&iss.version=off&iss.json=extended&analytics.cursor.columns=TOTAL&analytics.dates.columns=till
//
//https://iss.moex.com/iss/reference/


public class ExternalEndpoints {
    public static final String MOEX_API = "https://iss.moex.com";

    public static final String TICKER_REQUEST = "/iss/engines/stock/markets/shares/boards/TQBR/securities/";
    public static final String INDEX_REQUEST = "/iss/statistics/engines/stock/markets/index/analytics/IMOEX";
    public static final String INDEX10_REQUEST = "/iss/statistics/engines/stock/markets/index/analytics/MOEX10";

    public static final String TICKER_REQUEST_PARAMS = ".json?iss.meta=off&marketdata.columns=LAST,MARKETPRICE&iss.version=on&iss.json=extended&securities.columns=SECID,SHORTNAME,LOTSIZE&dataversion.columns=data_version";
    public static final String INDEX_REQUEST_PARAMS = ".json?iss.meta=off&analytics.columns=ticker,shortnames,weight&iss.version=off&iss.json=extended&analytics.cursor.columns=TOTAL&analytics.dates.columns=till&limit=100";
    public static final String INDEX10_REQUEST_PARAMS = ".json?iss.meta=off&analytics.columns=ticker,shortnames,weight&iss.version=off&iss.json=extended&analytics.cursor.columns=TOTAL&analytics.dates.columns=till";

    public static final String IMOEX_INDEX_URL = MOEX_API + INDEX_REQUEST + INDEX_REQUEST_PARAMS;
    public static final String INDEX10_URL = MOEX_API + INDEX10_REQUEST + INDEX10_REQUEST_PARAMS;
    public static final String ALL_TICKERS_URL = MOEX_API + TICKER_REQUEST + TICKER_REQUEST_PARAMS;
}

