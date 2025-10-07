package gg.techgarden.blog.cache.profile;

import gg.techgarden.blog.client.ProfileClient;
import gg.techgarden.blog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileClient profileClient;

    @Value("${app.profile.url:http://localhost:8082}")
    private String profileServiceUrl;

    public Profile updateProfile(Profile profile) {
        UUID sub = profile.getSub();
        if (!profileRepository.existsById(sub)) {
            profile = profileClient.profileClient()
                    .get()
                    .uri(profileServiceUrl + "/profiles?sub={sub}", sub)
                    .retrieve()
                    .body(Profile.class);
            if (profile == null) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Profile not found for sub: " + sub);
            }
        }
        return profileRepository.save(profile);
    }

    public Profile getCurrentUserProfile() {
        UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
        return profileRepository.findById(sub).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }
}
