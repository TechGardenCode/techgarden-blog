package gg.techgarden.blog.cache.profile;

import gg.techgarden.blog.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping("/{sub}")
    public Profile updateProfile(@PathVariable UUID sub, @RequestBody Profile profile) {
        if (profile.getSub() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Sub is required for update");
        }
        if (!sub.equals(profile.getSub())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Path Sub and Profile Sub do not match");
        }
        return profileService.updateProfile(profile);
    }
}
