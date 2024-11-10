package ru.sterkhovkv.IMOEX_screener.dto.MoexDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImoexIndexDTO {
    private ImoexIndexDTO.CharsetInfo charsetinfo;

    @JsonProperty("analytics")
    private List<ImoexIndexDTO.Analytics> analytics;

    @JsonProperty("analytics.cursor")
    private List<ImoexIndexDTO.Cursor> analyticsCursor;

    @JsonProperty("analytics.dates")
    private List<ImoexIndexDTO.DateRange> analyticsDates;

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
