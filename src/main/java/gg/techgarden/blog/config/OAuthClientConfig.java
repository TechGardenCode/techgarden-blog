package gg.techgarden.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class OAuthClientConfig {
    @Bean
    OAuth2AuthorizedClientService clientService(ClientRegistrationRepository repo) {
        return new InMemoryOAuth2AuthorizedClientService(repo);
    }

    @Bean
    OAuth2AuthorizedClientManager clientManager(ClientRegistrationRepository repo, OAuth2AuthorizedClientService svc) {
        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, svc);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }
}