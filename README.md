# POC Interceptor for RestClient (and RestTemplate)

This is a Spring Boot application that demonstrates the use of an interceptor to allow RestClient to be used for making Oauth2 calls.

It is related to this issue:
https://github.com/spring-projects/spring-security/issues/13588

It only demonstrates the client_credentials flow.
The interceptor for RestTemplate has the same signature so this can also be used for RestTemplate

I am mainly interested in microservice authn/authz in a financial environment which is why I wanted to use private_jwt (which is FAPI compliant) rather than password authentication for the client_credentials flow.

## How it works
When starting the application Spring Boot docker compose is used to start keycloak. Note: you need the "docker compose" plugin (v2), not "docker-compose" (v1).
Docker Compose is set to import my-realm with two clients: my-client and my-client-jwt
- my-client
  - client credentials flow
  - password based key
- my-client-jwt
  - client credentials flow (service account)
  - private_jwt see https://www.rfc-editor.org/rfc/rfc7523
  - uses callback Json Web Key Set:  http:/8081/localhost/jwks to be able to fetch the public key to authenticate (keycloak also supports loading a public key but using a jwks endpoint is more flexible and allows rotating the key at any time)
  
You can see the clients by logging into keycloak on http://localhost/8080 (username is admin, password is admin)

## interesting classes
### Client Config
All the beans for the Oauth2 clients. There are 6 clients 2 each (jwt and password) for WebClient, RestClient and RestTemplate.

### TestController
Has all the endpoints for making manual "tests". The 6 clients are injected here and each one may be called with a GET on a URL.
The clients each call the /target endpoint which logs the "authorization" header access token.

### OAuth2ClientInterceptor
The interceptor itself.

### jose/*
Anything in here is related to private jwt auth. Ignore it if you are not using this. 


## Trying it out
Run the application (uses maven). Will pull docker (may take some time) 
```shell
./mvnw spring-boot:run
```
The call each endpoint.
**There is no actual authentication anywhere**, we just log the Access Token on the `/target` endpoint.

```shell
curl http://localhost:8081/restClientPassword
curl http://localhost:8081/restClientJwt
curl http://localhost:8081/restTemplatePassword
curl http://localhost:8081/restTemplateJwt
```

Note there is also a scheduled task uses restClientJwt (previously it only worked from a serlet context and token caching was not working). 
see https://github.com/mjeffrey/spring-security-oauth2-restclient-interceptor/issues/1  
