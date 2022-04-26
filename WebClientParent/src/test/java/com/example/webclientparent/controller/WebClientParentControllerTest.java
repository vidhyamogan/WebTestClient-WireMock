package com.example.webclientparent.controller;

import model.Token;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
public class WebClientParentControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ApplicationContext applicationContext;


    @Before
    public void setUp()
    {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    //@Test
    public void getChildResponseTest() throws Exception {
        this.webTestClient.get().uri("http://localhost:8081/webClient/test")
                .header("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody("MockResponse".getClass());


        MvcResult msg = mockMvc.perform(
                get("/public/webclientparent/response")).andReturn();

    }

    @Test
    public void getResponse1()
    {
        this.webTestClient.get().uri(uriBuilder ->
            uriBuilder.path("/public/webclientparent/getUser")
                    .queryParam("name","sri")
                    .build())
                .accept(MediaType.ALL)
                .header("Content-Type","application/json;charset=UTF-8")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE);
    }

   // @Test
    /*public void getPostResponse() throws Exception {
        this.webTestClient
                .post()
                .uri("http://localhost:8081/webClient/postResponse")
                .bodyValue(new Token("12345"))
                .accept(MediaType.ALL)
                .header("content-type",MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);

        MvcResult msg = mockMvc.perform(
                get("/public/webclientparent/postResponse1")).andReturn();

    }*/
}
