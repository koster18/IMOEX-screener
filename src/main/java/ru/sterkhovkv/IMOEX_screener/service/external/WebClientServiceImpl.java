package ru.sterkhovkv.IMOEX_screener.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebClientServiceImpl implements WebClientService {
    private final WebClient webClient;

    @Override
    public <T> Mono<List<T>> fetch(String url, ParameterizedTypeReference<List<T>> responseType) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType);
    }
}
