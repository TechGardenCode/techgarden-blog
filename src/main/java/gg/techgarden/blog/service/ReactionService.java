package gg.techgarden.blog.service;

import gg.techgarden.blog.cache.profile.Profile;
import gg.techgarden.blog.cache.profile.ProfileService;
import gg.techgarden.blog.model.ReactionParentType;
import gg.techgarden.blog.model.ReactionType;
import gg.techgarden.blog.persistence.entity.Reaction;
import gg.techgarden.blog.persistence.repository.ReactionRepository;
import gg.techgarden.blog.util.SecurityUtil;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Reaction addReactionToPost(UUID postId, ReactionType reactionType) {
        if (reactionType == ReactionType.COMMENT) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Use addCommentToPost to add comments");
        }
        Profile profile = profileService.getCurrentUserProfile();
        Reaction reaction = new Reaction();
        reaction.setParentId(postId);
        reaction.setType(reactionType);
        reaction.setUser(profile);
        reaction.setParentType(ReactionParentType.POST);
        if (reactionRepository.existsByParentIdAndUserSubAndType(postId, profile.getSub(), reactionType)) {
            return null;
        }
        return reactionRepository.save(reaction);
    }

    public Reaction addCommentToPost(UUID postId, ReactionType reactionType, String content) {
        if (reactionType != ReactionType.COMMENT) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Only COMMENT reaction type is allowed for comments");
        }
        if (StringUtils.isBlank(content)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Content cannot be empty");
        }
        Profile profile = profileService.getCurrentUserProfile();
        Reaction reaction = new Reaction();
        reaction.setParentId(postId);
        reaction.setType(reactionType);
        reaction.setUser(profile);
        reaction.setParentType(ReactionParentType.POST);
        reaction.setContent(content);
        return reactionRepository.save(reaction);
    }

    public Page<Reaction> getCommentsForPost(UUID postId, Pageable pageable) {
        return reactionRepository.findAllByParentIdAndType(postId, ReactionType.COMMENT, pageable);
    }

    @Transactional
    public void removeReactionFromPost(UUID postId, ReactionType reactionType) {
        UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
        if (!reactionRepository.existsByParentIdAndUserSubAndType(postId, sub, reactionType)) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        reactionRepository.removeByParentIdAndUserSubAndType(postId, sub, reactionType);
    }

    @Transactional
    public void removeCommentFromPost(UUID postId, UUID reactionId) {
        UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
        Reaction reaction = reactionRepository.findById(reactionId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
        if (!reaction.getParentId().equals(postId) || !reaction.getUser().getSub().equals(sub) || reaction.getType() != ReactionType.COMMENT) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
        reactionRepository.delete(reaction);
    }
}
