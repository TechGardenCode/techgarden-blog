package gg.techgarden.blog.controller;

import gg.techgarden.blog.model.ReactionParentType;
import gg.techgarden.blog.model.ReactionType;
import gg.techgarden.blog.persistence.entity.Reaction;
import gg.techgarden.blog.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class ReactionController {
    private final ReactionService reactionService;

    @GetMapping("/posts/{parentId}/count")
    public Map<String, Long> getReactionCounts(@PathVariable UUID parentId) {
        return reactionService.getReactionCountsForPost(parentId);
    }

    @GetMapping("/posts/{parentId}/user-count")
    public List<String> getUserReactionCounts(@PathVariable UUID parentId) {
        return reactionService.getUserReactionCountsForPost(parentId);
    }

    @GetMapping("/posts/{parentId}/comments")
    public Page<Reaction> getCommentsForPost(@PathVariable UUID parentId, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reactionService.getCommentsForPost(parentId, pageable);
    }

    @PostMapping("/posts/{parentId}")
    public Reaction addReactionToPost(@PathVariable UUID parentId, @RequestParam ReactionType reactionType) {
        return reactionService.addReactionToPost(parentId, reactionType);
    }

    @PostMapping("/posts/{parentId}/comments")
    public Reaction addCommentToPost(@PathVariable UUID parentId, @RequestParam String content) {
        return reactionService.addCommentToPost(parentId, ReactionType.COMMENT, content);
    }

    @DeleteMapping("/posts/{parentId}/comments/{reactionId}")
    public void removeCommentFromPost(@PathVariable UUID parentId, @PathVariable UUID reactionId) {
        reactionService.removeCommentFromPost(parentId, reactionId);
    }

    @DeleteMapping("/posts/{parentId}")
    public void removeReactionFromPost(@PathVariable UUID parentId, @RequestParam ReactionType reactionType) {
        reactionService.removeReactionFromPost(parentId, reactionType);
    }
}
