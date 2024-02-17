package com.example.springsecurityrestclient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@AllArgsConstructor
public class CronService {

  private static final String TARGET = "http://localhost:8081/target";

  private RestClient restClientJwt;

  @Scheduled(cron = "0 */1 * * * *")
  public void performCronTask() {
    log.info("cronJob called");
    restClientJwt.get().uri(TARGET).retrieve().body(String.class);
  }
}
