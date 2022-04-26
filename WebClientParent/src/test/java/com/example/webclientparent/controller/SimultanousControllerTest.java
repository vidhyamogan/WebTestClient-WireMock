package com.example.webclientparent.controller;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import model.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment =SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class SimultanousControllerTest {

    private WireMockServer wireMockServer;

    private WebClient webClient;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    public void setUp()
    {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        webClient = WebClient.builder().baseUrl(wireMockServer.baseUrl()).build();
    }

    @AfterEach
    void tearDown() throws Exception{
        wireMockServer.stop();
    }

    @Test
    public void test() throws Exception {

        this.wireMockServer.stubFor(get("/token/"+1001)
                .willReturn(aResponse()
                        .withStatus(200)
                            .withBody(String.format("{\"tokenId\": %d }",1001))));


        MvcResult msg = mockMvc.perform(MockMvcRequestBuilders
                                .post("/public/block")
                                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"tokenId\": \"1001\" }"))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertNotNull(msg);
    }

    //@Test
    public void testParallel() throws Exception {
        int maxNum = 103;
        for(int i = 100 ;i<=maxNum;i++)
        {
            wireMockServer.stubFor(get("/token/"+i)
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(String.format("{\"tokenId\": %d }",i))));
        }

        List<Integer> tokenIds = IntStream
                .rangeClosed(100,maxNum)
                .boxed()
                .collect(Collectors.toList());


        MvcResult msg = mockMvc.perform(MockMvcRequestBuilders
                        .post("/public/parallel/tokens")
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .content("[100,110,120]"))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertNotNull(msg);
    }
}
