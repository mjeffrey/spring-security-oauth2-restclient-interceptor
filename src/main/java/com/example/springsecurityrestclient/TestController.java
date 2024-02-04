package com.example.springsecurityrestclient;

import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@AllArgsConstructor
public class TestController {

    private static final String TARGET = "http://localhost:8081/target";

    private WebClient webClientPassword;
    private WebClient webClientJwt;

    private RestClient restClientPassword;
    private RestClient restClientJwt;

    private RestTemplate restTemplatePassword;
    private RestTemplate restTemplateJwt;
    @GetMapping("/webClientPassword")
    public Mono<String> webClientPassword() {
        log.info("webClientPassword called");
        return webClientPassword.get().uri(TARGET).retrieve().bodyToMono(String.class);
    }

    @GetMapping("/webClientJwt")
    public Mono<String> webClientJwt() {
        log.info("webClientJwt called");
        return webClientJwt.get().uri(TARGET).retrieve().bodyToMono(String.class);
    }

    @GetMapping("/restClientPassword")
    public String restClientPassword() {
        log.info("restClientPassword called");
        return restClientPassword.get().uri(TARGET).retrieve().body(String.class);
    }

    @GetMapping("/restClientJwt")
    public String restClientJwt() {
        log.info("restClientJwt called");
        return restClientJwt.get().uri(TARGET).retrieve().body(String.class);
    }

    @GetMapping("/restTemplatePassword")
    public String callerRestTemplate() {
        log.info("restTemplatePassword called");
        return restTemplatePassword.getForObject(TARGET, String.class);
    }

    @GetMapping("/restTemplateJwt")
    public String restTemplateJwt() {
        log.info("restTemplateJwt called");
        return restTemplateJwt.getForObject(TARGET, String.class);
    }

    @SneakyThrows
    @GetMapping("/target")
    public String target(@RequestHeader HttpHeaders headers) {
        headers.forEach((key, value) -> {
            log.debug("Header '{}' = {}", key, value);
        });
        String token = headers.getFirst(HttpHeaders.AUTHORIZATION.toLowerCase()).substring(7);
        SignedJWT signedJWT = SignedJWT.parse(token);
        log.info("jwt {} {}", signedJWT.getHeader(), signedJWT.getJWTClaimsSet());
        return "called OK\n" ;
    }
}
