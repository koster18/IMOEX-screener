package ru.sterkhovkv.IMOEX_screener.dto.MoexDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Moex10IndexDTO {
    private Moex10IndexDTO.CharsetInfo charsetinfo;

    @JsonProperty("analytics")
    private List<Moex10IndexDTO.Analytics> analytics;

    @JsonProperty("analytics.cursor")
    private List<Moex10IndexDTO.Cursor> analyticsCursor;

    @JsonProperty("analytics.dates")
    private List<Moex10IndexDTO.DateRange> analyticsDates;

    @Data
    public static class CharsetInfo {
        private String name;
    }

    @Data
    public static class Analytics {
        private String ticker;
        private String shortnames;
        private double weight;
    }

    @Data
    public static class Cursor {
        @JsonProperty("TOTAL")
        private int total;
    }

    @Data
    public static class DateRange {
        private String till;
    }
}
