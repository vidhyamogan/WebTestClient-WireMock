package com.example.webclientparent.controller;

import model.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.management.ServiceNotFoundException;
import javax.naming.ServiceUnavailableException;

@RestController
@RequestMapping("/public/webclientparent")
public class WebClientParentController {

    //@Autowired
    WebClient webClient ;

    @GetMapping("/response")
    public Mono<HttpStatus> getChildResponse()
    {

        HttpStatus status = null;

        Mono<HttpStatus> result= webClient
                    .get()
                    .uri("/webClient/test")
                    .exchangeToMono(response -> {
                    if(response.statusCode().equals(HttpStatus.NOT_FOUND))
                        return response.bodyToMono(HttpStatus.class).thenReturn(response.statusCode());
                    else if(response.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR))
                        return response.bodyToMono(HttpStatus.class).thenReturn(response.statusCode());
                    else
                        return response.bodyToMono(HttpStatus.class).thenReturn(response.statusCode());
                    });

                return result;

                    /*.onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            response -> response.bodyToMono(String.class).map(ServiceNotFoundException::new))
                    .onStatus(
                            HttpStatus.INTERNAL_SERVER_ERROR::equals,
                            response -> response.bodyToMono(String.class).map(ServiceUnavailableException::new))
                    .bodyToMono(String.class);
            status = HttpStatus.OK;
        }catch (Exception ex)
        {
            if(ex instanceof  ServiceNotFoundException) status = HttpStatus.NOT_FOUND;
            else if(ex instanceof ServiceUnavailableException) status = HttpStatus.INTERNAL_SERVER_ERROR;
            else System.out.print("Unhandled exception");
        }*/





                 /*.onStatus(
                        HttpStatus.NOT_FOUND::equals,
                        response -> response.bodyToMono(String.class).map(Exception::new))
                 .onStatus(
                         HttpStatus.INTERNAL_SERVER_ERROR::equals,
                         response -> response.bodyToMono(String.class).map(Exception::new))
                .onStatus(
                        HttpStatus.OK::equals,
                        response -> response.bodyToMono(String.class).map()
                 )*/
                /*.bodyToMono(String.class);*/



    }

    /*@GetMapping("/postResponse1")
    public Mono<String> getPostResponse()
    {
        return  WebClient.create()
                .post()
                .uri("/private/webClient/postResponse")
                .bodyValue(new Token("12345"))
                .retrieve()
                .bodyToMono(String.class);
    }
*/
    @GetMapping("/response1")
    public ResponseEntity getChildResponse1() {

        HttpStatus status = null;
        ResponseEntity response = null;

        Mono<String> result = webClient
                .get()
                .uri("/webClient/test")
                .retrieve()
                .bodyToMono(String.class);
        response = new ResponseEntity(result, HttpStatus.valueOf(200));

        Mono<String> result1 = webClient
                .get()
                .uri("/webClient/test1")
                .retrieve()
                .bodyToMono(String.class);
        response = new ResponseEntity(result1, HttpStatus.valueOf(404));

        return response;
    }


    @GetMapping("/getUser")
    public ResponseEntity getChildResponse3(@RequestParam String name) {

        HttpStatus status = null;
        ResponseEntity response = null;

        System.out.println("Name===>"+name);

        Mono<String> result = webClient
                .get()
                .uri("/webClient/test")
                .retrieve()
                .bodyToMono(String.class);
        response = new ResponseEntity(result, HttpStatus.valueOf(200));

        return response;

    }


    

}
