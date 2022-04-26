package com.example.webclientparent.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.eclipse.jetty.http.HttpStatus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class ParrentControllerWireMockTest {

    private WireMockServer wireMockServer;

    private WebClient webClient;


    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebClientParentController controller;

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
    public void test1() throws Exception
    {
        wireMockServer.stubFor(get("/webClient/test")
                .willReturn(
                        WireMock.aResponse()
                        .withStatus(HttpStatus.OK_200)
                                .withBody("Test Result")
                ));



       Mono<String> response = webClient.get().uri("http://localhost:1911/webClient/test")
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(String.class);


        MvcResult msg = mockMvc.perform(
                        MockMvcRequestBuilders.get("/public/webclientparent/response"))
                .andExpect(status().isOk())
                .andReturn();

        //Assertions.assertEquals("Test Result",response);
        Assertions.assertNotNull(msg);
    }

    //@Test
    public void test2() throws Exception
    {
        wireMockServer.stubFor(get("/webClient/test")
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.NOT_FOUND_404)

                ));



       /*Mono<HttpStatus> status  = webClient.get().uri("/webClient/test")
                .accept(MediaType.ALL)
                .retrieve()
                .bodyToMono(HttpStatus.class);

       // Assertions.assertEquals(HttpStatus.NOT_FOUND_404,status);*/



        MvcResult msg = mockMvc.perform(
                        MockMvcRequestBuilders.get("/public/webclientparent/response"))
                .andExpect(status().isOk())
                .andReturn();

    }

    //@Test
    public void test3() throws Exception
    {
        wireMockServer.stubFor(get("/webClient/test1")
                .willReturn(
                        WireMock.aResponse()
                                .withStatus(HttpStatus.NOT_FOUND_404)

                ));


        MvcResult msg = mockMvc.perform(
                        MockMvcRequestBuilders.get("/public/webclientparent/response1"))
                .andExpect(status().is4xxClientError())
                .andReturn();

    }




}
