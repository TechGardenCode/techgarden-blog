package gg.techgarden.blog.cache.profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private final ProfileRepository profileRepository;

    public Profile updateProfile(Profile profile) {
        if (!profileRepository.existsById(profile.getSub())) {
            log.debug("No profile found with id {}", profile.getSub());
            return null;
        }
        return profileRepository.save(profile);
    }
}
