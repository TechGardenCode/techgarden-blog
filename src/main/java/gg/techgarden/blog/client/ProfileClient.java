package gg.techgarden.blog.client;

import gg.techgarden.blog.cache.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ProfileClient {

    private final OAuth2AuthorizedClientManager manager;
    private final RestClient.Builder builder;

    private String token() {
        var req = OAuth2AuthorizeRequest.withClientRegistrationId("svc-blog")
                .principal("svc-blog") // any non-null principal key for cache keying
                .build();
        var client = manager.authorize(req);
        OAuth2AccessToken at = client.getAccessToken();
        return at.getTokenValue();
    }

    public void getUserProfile(String baseUrl, String sub) {
        var rest = builder
                .requestInterceptor((request, body, exec) -> {
                    request.getHeaders().setBearerAuth(token());
                    return exec.execute(request, body);
                })
                .build();

        rest.get()
                .uri(baseUrl + "/profiles?sub=", sub)
                .retrieve()
                .toEntity(Profile.class);
    }

    public RestClient profileClient() {
        return builder
                .requestInterceptor((request, body, exec) -> {
                    request.getHeaders().setBearerAuth(token());
                    return exec.execute(request, body);
                })
                .build();
    }
}