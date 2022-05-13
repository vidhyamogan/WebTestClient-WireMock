package com.example.webclientparent.controller;

import com.example.webclientparent.model.TokenWrapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.netty.handler.timeout.ReadTimeoutException;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    //Test single api call - with success
    @Test
    void test1()
    {
        wireMockServer.stubFor(
                WireMock.get("/token/100")
                        .willReturn(WireMock.aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("block-api/response-200.json"))
        );

       WebTestClient.ResponseSpec responseSpec = this.webTestClient
                .get()
                .uri("/public/block")
                .exchange()
                .expectStatus().isOk();

       Token token = Mono.from(responseSpec.returnResult(Token.class).getResponseBody()).block();
       Assertions.assertEquals(1001,token.getTokenId());


    }


    //Test single api call - with 400
    @Test
    void test_400()
    {
        wireMockServer.stubFor(
                WireMock.get("/token/100")
                        .willReturn(WireMock.aResponse()
                                .withStatus(400)
                                .withBody("Not Found Error from test case")
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE))

        );

        this.webTestClient
                .get()
                .uri("/public/block")
                .exchange()
                .expectStatus().is4xxClientError();


    }

    //Test single api call - with 500
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

    //Test multiple api call - with success - sequentailly
    @Test
    public void test2() throws Exception {
        int maxNum = 103;
        for(int i = 100 ;i<=maxNum;i++)
        {
            wireMockServer.stubFor(get("/token/"+i)
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(200)
                            .withBodyFile("block-api/responseTokenArray.json"))
            );
        }

        List<Integer> tokenIds = IntStream
                .rangeClosed(100,maxNum)
                .boxed()
                .collect(Collectors.toList());


        WebTestClient.ResponseSpec responseSpec = this.webTestClient
                        .post().uri("/public/tokens")
                        .bodyValue(tokenIds).exchange()
                        .expectStatus().isOk();
       //TokenWrapper tokenList = responseSpec.expectBody(TokenWrapper.class).returnResult().getResponseBody();
       //Assertions.assertEquals(301,tokenList.getToken().get(0).getTokenId());

    }


    //out of 4 call Last call failed with 400 - Mono.error will be thrown
    // and subsequent calls will be dismissed
    @Test
    public void test_mutipleCalls() throws Exception {
        int maxNum = 103;
        for(int i = 100 ;i<maxNum;i++)
        {
            wireMockServer.stubFor(get("/token/"+i)
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(200)
                            .withBodyFile("block-api/response-200.json"))
            );
        }

        wireMockServer.stubFor(get("/token/"+103)
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(400))
        );

        List<Integer> tokenIds = IntStream
                .rangeClosed(100,maxNum)
                .boxed()
                .collect(Collectors.toList());


        this.webTestClient
                .post().uri("/public/tokens")
                .bodyValue(tokenIds).exchange()
                .expectStatus().is4xxClientError()
                .expectBody();//check the content of the bad request

    }

    //out of 4 call 2nd call failed with 500 - Mono.error will be thrown
    //and subsequent calls will be continued and respond with 500
    @Test
    public void test_mutipleCalls500() throws Exception {
        int maxNum = 103;

        for(int i = 100 ;i<maxNum;i++)
    {
            int status = 200;
            if(i==102)
                status = 500;

            wireMockServer.stubFor(get("/token/"+i)
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(status))
            );

        }

        List<Integer> tokenIds = IntStream
                .rangeClosed(100,maxNum)
                .boxed()
                .collect(Collectors.toList());


        this.webTestClient
                .post().uri("/public/tokens")
                .bodyValue(tokenIds).exchange()
                .expectStatus().is5xxServerError();

    }

    //Test multiple api call - when it reached 101 got read timeout exception
    @Test
    public void test_ParllelCalls() throws Exception {
        int maxNum = 103;

        for(int i = 100 ;i<=maxNum;i++)
        {
            if(i==101)
            {
                wireMockServer.stubFor(get("/token/"+i)
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withFixedDelay(500000))
                );//when it reached 101 got read timeout exception
            }else {
                wireMockServer.stubFor(get("/token/"+i)
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withStatus(200))
                );
            }

        //check the size of tokens****************
        }

        List<Integer> tokenIds = IntStream
                .rangeClosed(100,maxNum)
                .boxed()
                .collect(Collectors.toList());


        WebClientRequestException exception = assertThrows(WebClientRequestException.class,()->{
            this.webTestClient
                    .post().uri("/public/tokens")
                    .bodyValue(tokenIds)
                    .exchange();
        });
        System.out.println("exception messgae"+ exception.getHeaders());
        assertTrue(exception.getMessage().contains("ReadTimeoutException"));

    }

    @Test // when it reaches 201 -- it has 500 response inbuilt
    // and it will execute all other request and final outcome is 500
    public void test_ParllelCalls_500() throws Exception {
        int maxNum = 202;

        for(int i = 200 ;i<=maxNum;i++)
        {
            int status =200;
            if(i==201) status=500;
            wireMockServer.stubFor(get("/token/"+i)
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(status))
            );

        }

        List<Integer> tokenIds = IntStream
                .rangeClosed(200,maxNum)
                .boxed()
                .collect(Collectors.toList());


        this.webTestClient
                .post().uri("/public/gatherAllObjects")
                .bodyValue(tokenIds).exchange()
                .expectStatus().is5xxServerError();

    }


    @Test //test  all api in parllel - and check for 200
    public void test_mutipleParllelCalls() throws Exception {
        int maxNum = 103;

        for(int i = 100 ;i<=maxNum;i++)
        {
            wireMockServer.stubFor(get("/token/"+i)
                    .willReturn(aResponse()
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(200))
            );

        }

        List<Integer> tokenIds = IntStream
                .rangeClosed(100,maxNum)
                .boxed()
                .collect(Collectors.toList());


        this.webTestClient
                .post().uri("/public/parallel/tokens")
                .bodyValue(tokenIds).exchange()
                .expectStatus().isOk();

    }

    //deplay the api with 50ms and webclient is configured 10ms - so ReadTimeOutException will be thrown
    @Test
    void test_Delay()
    {
        wireMockServer.stubFor(
                WireMock.get("/token/100")
                        .willReturn(WireMock
                                .aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withFixedDelay(500000)
                                .withBodyFile("block-api/response-200.json"))
        );


        WebClientRequestException exception = assertThrows(WebClientRequestException.class,()->{
            this.webTestClient
                    .get()
                    .uri("/public/exception")
                    .exchange();
        });
       System.out.println("exception messgae"+ exception.getHeaders());
       assertTrue(exception.getMessage().contains("ReadTimeoutException"));

    }

    //single api is mocked and splunk api is not mocked so the test
    // will fail because splunk api is not up and running
    @Test
    void test_logEvent()
    {
        wireMockServer.stubFor(
                WireMock.get("/token/100")
                        .willReturn(WireMock.aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBodyFile("block-api/response-200.json"))
        );

        this.webTestClient
                .get()
                .uri("/public/logEvent")
                .exchange()
                .expectStatus().isOk();


    }


    /*@Test
    void test_Error()
    {
        wireMockServer.stubFor(
                WireMock.get("/token/100")
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withStatus(400)
                                .withBodyFile("block-api/responseToken-404.json"))

        );

        this.webTestClient
                .get()
                .uri("/public/errors")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo(400);
    }*/


    @Test //to test differernt api with different response and check the size of it
    public void test_ParallelerrorResume() throws Exception {

        wireMockServer.stubFor(get("/error/token/" + 301)
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBodyFile("block-api/response-200.json"))

        );

        wireMockServer.stubFor(get("/error/token/" + 302)
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(404))
        );

        wireMockServer.stubFor(get("/error/token/" + 303)
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(500))
        );

        List<Integer> tokenIds = IntStream
                .rangeClosed(301,303)
                .boxed()
                .collect(Collectors.toList());


       WebTestClient.ListBodySpec listBodySpec = this.webTestClient
                .post().uri("/public/gatherError/tokens")
                .bodyValue(tokenIds).exchange()
                .expectStatus().isOk()
                .expectBodyList(Token.class)
                .hasSize(3);


    }


    // testcase to log the error request to logs  and give 200 response
    @Test
    public void test_donOnErrorwithrequestLog()
    {
        wireMockServer.stubFor(get("/error/token/" + 1001)
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(404)
                        .withBodyFile("block-api/responseToken-404.json"))

        );

        wireMockServer.stubFor(post("/errorLogRequest")
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200))

        );

        WebTestClient.ResponseSpec response = this.webTestClient.get().uri(uriBuilder ->
                        uriBuilder.path("/public/doOnError")
                                .queryParam("id",1001)
                                .build())
                .accept(MediaType.ALL)
                .header("Content-Type","application/json;charset=UTF-8")
                .exchange()
                .expectStatus().is4xxClientError();



//objectMapper check for the object value
    }





}
