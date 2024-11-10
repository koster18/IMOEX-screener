package ru.sterkhovkv.IMOEX_screener.dto.MoexDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerMoexDTO {
    private TickerMoexDTO.CharsetInfo charsetinfo;

    @JsonProperty("securities")
    private List<TickerMoexDTO.Securities> securities;

    @JsonProperty("marketdata")
    private List<TickerMoexDTO.Marketdata> marketdata;

    @JsonProperty("dataversion")
    private List<TickerMoexDTO.Dataversion> dataversion;

    @Data
    public static class CharsetInfo {
        private String name;
    }

    @Data
    public static class Securities {
        @JsonProperty("SECID")
        private String ticker;
        @JsonProperty("SHORTNAME")
        private String shortnames;
        @JsonProperty("LOTSIZE")
        private int lotsize;
    }

    @Data
    public static class Marketdata {
        @JsonProperty("LAST")
        private double price;

        @JsonProperty("MARKETPRICE")
        private double price_last;
    }

    @Data
    public static class Dataversion {
        private int data_version;
    }
}
