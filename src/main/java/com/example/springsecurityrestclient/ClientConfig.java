/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.springsecurityrestclient;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInitializer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfig {

    @Bean
    RestClient restClientPassword(RestClient.Builder builder,
                          OAuth2AuthorizedClientManager authorizedClientManager,
                          ClientRegistrationRepository clientRegistrationRepository) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("secret");
        ClientHttpRequestInterceptor interceptor = new OAuth2ClientInterceptor(authorizedClientManager, clientRegistration);
        return builder.requestInterceptor(interceptor).build();
    }

    /**
     * This uses requestInitializer (just as a demonstration - we could use interceptor as well)
     */
    @Bean
    RestClient restClientJwt(RestClient.Builder builder,
                          OAuth2AuthorizedClientManager authorizedClientManager,
                          ClientRegistrationRepository clientRegistrationRepository) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("jwt");
        ClientHttpRequestInitializer initializer = new OAuth2ClientInterceptor(authorizedClientManager, clientRegistration);
        return builder.requestInitializer(initializer).build();
    }


    @Bean
    public RestTemplate restTemplatePassword(RestTemplateBuilder restTemplateBuilder,
                                             OAuth2AuthorizedClientManager authorizedClientManager,
                                             ClientRegistrationRepository clientRegistrationRepository) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("secret");
        return restTemplateBuilder
                .additionalInterceptors(new OAuth2ClientInterceptor(authorizedClientManager, clientRegistration))
                .build();
    }

    @Bean
    public RestTemplate restTemplateJwt(RestTemplateBuilder restTemplateBuilder,
                                        OAuth2AuthorizedClientManager authorizedClientManager,
                                        ClientRegistrationRepository clientRegistrationRepository) {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("jwt");
        return restTemplateBuilder
                .additionalInterceptors(new OAuth2ClientInterceptor(authorizedClientManager, clientRegistration))
                .build();
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> responseClient,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
//						.authorizationCode()
                        .clientCredentials(clientCredentials ->
                                clientCredentials.accessTokenResponseClient(responseClient))
                        .build();

        DefaultOAuth2AuthorizedClientManager clientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        clientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return clientManager;
    }

//    @Bean
//    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
//        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
//    }

}
