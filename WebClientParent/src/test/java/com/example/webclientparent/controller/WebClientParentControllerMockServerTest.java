package com.example.webclientparent.controller;

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import model.Token;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@SpringBootTest
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("local")
public class WebClientParentControllerMockServerTest {

    public static MockWebServer mockserver;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    //@Qualifier("mockWebClient")
    private WebClient webClient;


    @BeforeAll
    public static void setUp() throws IOException {
        mockserver = new MockWebServer();
        mockserver.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockserver.shutdown();
    }

    @Configuration
    public static class TestConfig{
        @Bean(name = "mockWebClient")
        WebClient creatWebClient()
        {
            HttpUrl url = mockserver.url("/");
            WebClient webClient = WebClient.create(url.toString());
            return webClient;
        }
    }


    //@Test
    public void getChildResponseTest() throws Exception {

        mockserver.enqueue(new MockResponse().
        setBody("Test Response")
        .setResponseCode(200)
        .addHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE));

        this.webClient.get().uri("/private/webClient/test");
        /*System.out.println("childMsg" +childMsg);*/
        MvcResult msg = mockMvc.perform(
                get("/public/webclientparent/response"))
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertNotNull(msg);

    }

    @Test
    public void getChildResponseTest400() throws Exception {

        //MockWebServer mockWebServer = new MockWebServer();
        this.mockserver.enqueue(new MockResponse().setResponseCode(400));


       this.webClient.get()
                .uri("private/webClient/test")
               .retrieve()
               .onStatus(status->status.equals("400"), ClientResponse::createException);


        MvcResult msg = mockMvc.perform(
                get("/public/webclientparent/response"))
                .andExpect(status().is4xxClientError())
                .andReturn();

        Assert.assertNotNull(msg);

    }

    //@Test
    public void getChildResponseTest500() throws Exception {

        mockserver.enqueue(new MockResponse().setResponseCode(500));

        this.webClient.get()
                .uri("private/webClient/test")
                .retrieve();


        MvcResult msg = mockMvc.perform(
                        get("/public/webclientparent/response"))
                .andExpect(status().is5xxServerError())
                .andReturn();

        Assert.assertNotNull(msg);

    }

    /*//@Test
    public void getPostResponse() throws Exception {

        mockserver.enqueue(new MockResponse().setResponseCode(200).setBody("12345"));

        this.webClient.post()
                .uri("private/webClient/postResponse")
                .bodyValue(new Token("12345"))
                .header("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.ALL)
                .retrieve();


        MvcResult msg = mockMvc.perform(
                get("/public/webclientparent/postResponse1"))
                .andExpect(status().isOk()).andReturn();

    }*/
}
