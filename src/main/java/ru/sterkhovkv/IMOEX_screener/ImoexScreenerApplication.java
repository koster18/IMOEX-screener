package ru.sterkhovkv.IMOEX_screener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.sterkhovkv.IMOEX_screener.service.StockService;

import java.sql.Timestamp;

@SpringBootApplication
@Slf4j
public class ImoexScreenerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImoexScreenerApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(StockService stockService) {
		return args -> {
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			stockService.updateStockTickersDBIndexFromMoex();
			log.info("Загрузка индексов и цен выполнена за: {} мс", (System.currentTimeMillis() - timestamp.getTime()));
		};
	}
}
