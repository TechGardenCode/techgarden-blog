package gg.techgarden.blog.controller;

import gg.techgarden.blog.model.ReactionType;
import gg.techgarden.blog.service.ReactionService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/posts/{parentId}")
    public void addReactionToPost(@PathVariable UUID parentId, @RequestParam ReactionType reactionType) {
        reactionService.addReactionToPost(parentId, reactionType);
    }

    @DeleteMapping("/posts/{parentId}")
    public void removeReactionFromPost(@PathVariable UUID parentId, @RequestParam ReactionType reactionType) {
        reactionService.removeReactionFromPost(parentId, reactionType);
    }
}
