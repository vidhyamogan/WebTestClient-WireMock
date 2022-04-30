package com.example.webclientparent.controller;


import model.LogEvent;
import model.Token;
import org.springframework.beans.factory.annotation.Autowired;
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
                .retrieve().onStatus(HttpStatus::is4xxClientError,
                clientResponse ->
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found error")))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse ->
                                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error")))
                .bodyToMono(Token.class).block();

    }


    /* WebClient has timeout of 10000 ,
     if request is not received within the specified time WebClientRequestException will be thrown*/

    @GetMapping("/exception")
    public Token getException()
    {
        Mono<Token> token;
        token = this.customWebClient.get()
                .uri("/token/{id}", 100)
                .retrieve().bodyToMono(Token.class);
        return token.block();
    }


    @GetMapping("/block")
    public Token getToken()
    {
        Token token = this.customWebClient.get()
                .uri("/token/{id}", 100)
                .retrieve().onStatus(HttpStatus::is4xxClientError,
                        clientResponse ->
                                Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found error")))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse ->
                                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error")))
                .bodyToMono(Token.class)
                .block();




        return token;
    }


    @GetMapping("/logEvent")
    public Token getLogEvent()
    {
        Token token = this.customWebClient.get()
                .uri("/token/{id}", 100)
                .retrieve().onStatus(HttpStatus::is4xxClientError,
                        clientResponse ->
                                Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Not found error")))
                .onStatus(HttpStatus::is5xxServerError,
                        clientResponse ->
                                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Internal Server Error")))
                .bodyToMono(Token.class)
                .block();


        //tHIS IS simple call for the request which is not mocked or stubbed
        String logEvent = WebClient.builder().build().get()
            .uri("http://splunklog1.comlogRequest")
            .retrieve().bodyToMono(String.class)
            .block();
        System.out.println("Log Event===>"+ logEvent);


        return token;
    }




































}
