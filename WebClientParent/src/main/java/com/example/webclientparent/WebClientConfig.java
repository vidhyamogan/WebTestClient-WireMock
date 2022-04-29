package com.example.webclientparent;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Value("${base_url}") String baseUrl;

    @Bean
    public WebClient customWebClient(WebClient.Builder webClientBuilder)
    {
        return webClientBuilder
                .baseUrl(baseUrl)
                .clientConnector(getClientConnector())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


    private ReactorClientHttpConnector getClientConnector(){
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,10)
                .doOnConnected(connection -> connection.addHandler(new ReadTimeoutHandler(10))
        );

        return new ReactorClientHttpConnector(httpClient);
    }


}
