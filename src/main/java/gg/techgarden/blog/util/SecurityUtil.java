package gg.techgarden.blog.util;

import gg.techgarden.blog.cache.profile.Profile;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor
public final class SecurityUtil {

    /**
     * Gets the current user's subject (sub claim) from the JWT.
     */
    public static Optional<UUID> getCurrentUserSub() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(UUID.fromString(jwt.getSubject()));
        }

        return Optional.empty();
    }

    public static Optional<Profile> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Profile profile = new Profile();
            profile.setSub(UUID.fromString(jwt.getSubject()));
            profile.setEmail(jwt.getClaimAsString("email"));
            profile.setDisplayName(jwt.getClaimAsString("name"));
            return Optional.of(profile);
        }

        return Optional.empty();
    }

    /**
     * Gets any claim from the JWT by key.
     */
    public static Optional<Object> getClaim(String claimName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getClaim(claimName));
        }

        return Optional.empty();
    }
}