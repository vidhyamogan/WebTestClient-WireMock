package com.example.webclientparent.controller;


import model.Token;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public")
public class WebClientSimultanousController {



    /*//@Autowired
    WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();*/

    private final WebClient customWebClient;

    public WebClientSimultanousController(WebClient customWebClient) {
        this.customWebClient = customWebClient;
    }


    @PostMapping("/tokens")
    public List<Token> getTokens(@RequestBody List<Integer> tokenIds)
    {
         return this.fetchTokens(tokenIds);
    }

    @PostMapping("/parallel/tokens")
    public ParallelFlux<Token> getParallelTokens(@RequestBody List<Integer> tokenIds)
    {
        return this.fetchTokensParallel(tokenIds);
    }

    //squential calls upto 256 default no#, took 12sec to complete all the calls
    public List<Token> fetchTokens(List<Integer> tokenIds) {
        /*return Flux.fromIterable(tokenIds)
                .flatMap(this::getToken);*/

        return tokenIds.stream().map(this::getToken).collect(Collectors.toList());
    }

    //parallel calls , took 375ms to complete the same no of calls in fetchTokens ()
    public ParallelFlux<Token> fetchTokensParallel(List<Integer> tokenIds) {
        return Flux.fromIterable(tokenIds)
                .parallel()
                .map(this::getToken);
    }

   /* public Mono<Token> getToken(int id) {
        return webClient.get()
                .uri("http://localhost:9898/token/{id}", id)
                .retrieve()
                .bodyToMono(Token.class);
    }*/

    public Token getToken(int id) {
        return this.customWebClient.get()
                .uri("/token/{id}", id)
                .retrieve()
                .bodyToMono(Token.class).block();
    }

    @GetMapping("/block")
    public Token getToken()
    {
        return this.customWebClient.get()
                .uri("/token/{id}", 100)
                .retrieve().onStatus(HttpStatus::is4xxClientError,
                        clientResponse ->
                                Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found error")))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse ->
                                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error")))
                .bodyToMono(Token.class)
                .block();
    }






}
