package ru.sterkhovkv.IMOEX_screener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sterkhovkv.IMOEX_screener.model.StockTicker;

import java.util.List;

public interface StockTickerRepository extends JpaRepository<StockTicker, Integer> {
    StockTicker findFirstByTicker(String ticker);
    List<StockTicker> findByCountInPortfolioAndWeightImoexAndWeightMoex10(int countInPortfolio, double weightImoex, double weightMoex10);
}
