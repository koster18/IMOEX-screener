package ru.sterkhovkv.IMOEX_screener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sterkhovkv.IMOEX_screener.model.StockTicker;

import java.util.List;

public interface StockTickerRepository extends JpaRepository<StockTicker, Integer> {

    StockTicker findFirstByTicker(String ticker);

    List<StockTicker> findByCountInPortfolioAndWeightImoexAndWeightMoex10(int countInPortfolio, double weightImoex, double weightMoex10);

    @Query("SELECT s FROM StockTicker s WHERE s.ticker IN :tickers")
    List<StockTicker> findByTickers(@Param("tickers") List<String> tickers);

}
