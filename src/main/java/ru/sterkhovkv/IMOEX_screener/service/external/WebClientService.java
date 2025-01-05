package ru.sterkhovkv.IMOEX_screener.service.external;

import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WebClientService {
    <T> Mono<List<T>> fetch(String url, ParameterizedTypeReference<List<T>> responseType);
}
