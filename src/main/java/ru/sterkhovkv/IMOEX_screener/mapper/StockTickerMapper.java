package ru.sterkhovkv.IMOEX_screener.mapper;

import org.springframework.stereotype.Component;
import ru.sterkhovkv.IMOEX_screener.dto.StockTickerDTO;
import ru.sterkhovkv.IMOEX_screener.model.StockTicker;

@Component
public class StockTickerMapper {

    public StockTicker toEntity(StockTickerDTO dto) {
        if (dto == null) {
            return null;
        }
        return StockTicker.builder()
                .ticker(dto.getTicker())
                .shortname(dto.getShortname())
                .lotsize(dto.getLotsize())
                .price(dto.getPrice())
                .weightImoex(dto.getWeightImoex())
                .weightMoex10(dto.getWeightMoex10())
                .customWeight(1.00)
                .countInPortfolio(0)
                .build();
    }

    public StockTickerDTO toDTO(StockTicker entity) {
        if (entity == null) {
            return null;
        }
        return StockTickerDTO.builder()
                .ticker(entity.getTicker())
                .shortname(entity.getShortname())
                .lotsize(entity.getLotsize())
                .price(entity.getPrice())
                .weightImoex(entity.getWeightImoex())
                .weightMoex10(entity.getWeightMoex10())
                .build();
    }

    public void updateEntity(StockTicker entity, StockTickerDTO dto) {
        if (dto != null && entity != null) {
            entity.setShortname(dto.getShortname());
            entity.setLotsize(dto.getLotsize());
            entity.setPrice(dto.getPrice());
            entity.setWeightImoex(dto.getWeightImoex());
            entity.setWeightMoex10(dto.getWeightMoex10());
        }
    }
}


