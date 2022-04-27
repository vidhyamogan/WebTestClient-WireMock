package com.example.webclientparent.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import model.Token;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlockTest {

    @Autowired
    private WebTestClient webTestClient;

    private static WireMockServer wireMockServer;

    @Autowired
    WebApplicationContext applicationContext;

    @DynamicPropertySource
    static void overrideUrl(DynamicPropertyRegistry dynamicPropertyRegistry)
    {
        dynamicPropertyRegistry.add("base_url",wireMockServer::baseUrl);
    }


    @BeforeAll
    static void startWireMockServer()
    {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock(){
        wireMockServer.stop();
    }

    @BeforeEach
    public void setUpWebTestClient()
    {
        webTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    void test1()
    {
        wireMockServer.stubFor(
                WireMock.get("/token/100")
                        .willReturn(WireMock.aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("block-api/response-200.json"))
        );

        this.webTestClient
                .get()
                .uri("/public/block")
                .exchange()
                .expectStatus().isOk();


    }

    @Test
    void test_400()
    {
        wireMockServer.stubFor(
                WireMock.get("/token/100")
                        .willReturn(WireMock.aResponse()
                                .withStatus(400)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE))

        );

        this.webTestClient
                .get()
                .uri("/public/block")
                .exchange()
                .expectStatus().is4xxClientError();


    }

    @Test
    void test_500()
    {
        wireMockServer.stubFor(
                WireMock.get("/token/100")
                        .willReturn(WireMock.aResponse()
                                .withStatus(500)
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE))

        );

        this.webTestClient
                .get()
                .uri("/public/block")
                .exchange()
                .expectStatus().is5xxServerError();


    }

    @Test
    public void test2() throws Exception {
        int maxNum = 103;
        for(int i = 100 ;i<=maxNum;i++)
        {
            wireMockServer.stubFor(get("/token/"+i)
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(200)
                            .withBodyFile("block-api/response-200.json"))
            );
        }

        List<Integer> tokenIds = IntStream
                .rangeClosed(100,maxNum)
                .boxed()
                .collect(Collectors.toList());


        this.webTestClient
                        .post().uri("/public/tokens")
                        .bodyValue(tokenIds).exchange()
                        .expectStatus().isOk();

    }


}
