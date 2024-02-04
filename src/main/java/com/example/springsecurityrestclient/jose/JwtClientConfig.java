package com.example.springsecurityrestclient.jose;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.function.Function;

@Configuration
@Slf4j
public class JwtClientConfig {

    @Bean
    JWKSource<SecurityContext> jwkSource() {
        JWK key = JwkUtils.generateEc();
        JWKSet jwkSet = new JWKSet(key);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    Function<ClientRegistration, JWK> jwkResolver(JWKSource<SecurityContext> jwkSource) {
        JWKSelector jwkSelector = new JWKSelector(new JWKMatcher.Builder().privateOnly(true).build());
        return (registration) -> getJwk(jwkSource, jwkSelector);
    }

    private JWK getJwk(JWKSource<SecurityContext> jwkSource, JWKSelector jwkSelector) {
        JWKSet jwkSet = null;
        try {
            jwkSet = new JWKSet(jwkSource.get(jwkSelector, null));
        } catch (KeySourceException ex) {
            log.error("cannot locate private key", ex);
        }
        return jwkSet != null ? jwkSet.getKeys().iterator().next() : null;
    }

    @Bean
    OAuth2AccessTokenResponseClient<OAuth2ClientCredentialsGrantRequest> clientCredentialsTokenResponseClient(
            Function<ClientRegistration, JWK> jwkResolver) {

        OAuth2ClientCredentialsGrantRequestEntityConverter clientCredentialsGrantRequestEntityConverter = new OAuth2ClientCredentialsGrantRequestEntityConverter();
        clientCredentialsGrantRequestEntityConverter.addParametersConverter(new NimbusJwtClientAuthenticationParametersConverter<>(jwkResolver));

        clientCredentialsGrantRequestEntityConverter.addParametersConverter(authorizationGrantRequest -> {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.add(OAuth2ParameterNames.CLIENT_ID, authorizationGrantRequest.getClientRegistration().getClientId());
            return parameters;
        });

        DefaultClientCredentialsTokenResponseClient clientCredentialsTokenResponseClient = new DefaultClientCredentialsTokenResponseClient();
        clientCredentialsTokenResponseClient.setRequestEntityConverter(clientCredentialsGrantRequestEntityConverter);

        return clientCredentialsTokenResponseClient;
    }

}
