package gg.techgarden.blog.service;

import gg.techgarden.blog.cache.profile.Profile;
import gg.techgarden.blog.cache.profile.ProfileService;
import gg.techgarden.blog.model.ReactionParentType;
import gg.techgarden.blog.model.ReactionType;
import gg.techgarden.blog.persistence.entity.Reaction;
import gg.techgarden.blog.persistence.repository.ReactionRepository;
import gg.techgarden.blog.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final ProfileService profileService;

    public Map<String, Long> getReactionCountsForPost(UUID postId) {
        return reactionRepository.findReactionCountByPostId(postId).stream()
                .collect(Collectors.toMap(
                        tuple -> ((String) tuple.get(0)),
                        tuple -> ((Number) tuple.get(1)).longValue()
                ));
    }

    public List<String> getUserReactionCountsForPost(UUID postId) {
        UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
        return reactionRepository.findReactionCountByPostIdAndUserId(postId, sub);
    }

    public void addReactionToPost(UUID postId, ReactionType reactionType) {
        Profile profile = profileService.getCurrentUserProfile();
        Reaction reaction = new Reaction();
        reaction.setParentId(postId);
        reaction.setType(reactionType);
        reaction.setUser(profile);
        reaction.setParentType(ReactionParentType.POST);
        if (reactionRepository.existsByParentIdAndUserSubAndType(postId, profile.getSub(), reactionType)) {
            return;
        }
        reactionRepository.save(reaction);
    }

    @Transactional
    public void removeReactionFromPost(UUID postId, ReactionType reactionType) {
        UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
        if (!reactionRepository.existsByParentIdAndUserSubAndType(postId, sub, reactionType)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        reactionRepository.removeByParentIdAndUserSubAndType(postId, sub, reactionType);
    }
}
