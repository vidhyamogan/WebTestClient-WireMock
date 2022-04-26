package com.example.webclientparent.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import model.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment =  RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT36S")
@AutoConfigureMockMvc
public class WebClientParentControllerWireMockTest {

   @Autowired
   private WebTestClient webTestClient;


   @Autowired
   MockMvc mockMvc;

   @RegisterExtension
   static WireMockExtension wireMockServer = WireMockExtension.newInstance()
           .options(wireMockConfig().dynamicPort())
           .build();

   /*@DynamicPropertySource
   static void congigureProerties(DynamicPropertyRegistry registry)
   {
       registry.add("/",wireMockServer::baseUrl);
   }*/



   @AfterEach
    void resetAll(){
       wireMockServer.resetAll();
   }

   @Test
    void testGetChildResponse() throws Exception {
       wireMockServer.stubFor(
               WireMock.get(WireMock.urlEqualTo("/webClient/test"))
                .willReturn(aResponse()
                        .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200))
       );

       //the below code will work when the both parent and child application is up
       /*this.webTestClient
               .get()
               .uri("/public/webclientparent/response")
               .exchange()
               .expectStatus().isOk();*/

      //This also working if appication is up
      /*MvcResult msg = mockMvc.perform(
                      get("/public/webclientparent/response"))
              .andExpect(status().isOk())
              .andReturn();
*/



   }

}
