package gg.techgarden.blog.controller;

import gg.techgarden.blog.model.TopPostTimeframe;
import gg.techgarden.blog.persistence.entity.Post;
import gg.techgarden.blog.persistence.entity.PostMetadata;
import gg.techgarden.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;

    @GetMapping("/discover")
    public Page<PostMetadata> getDiscoverFeed() {
        // todo: implement discover feed logic
        return Page.empty();
    }

    @GetMapping("/following")
    public Page<PostMetadata> getFollowingFeed() {
        // todo: implement following feed logic
        return Page.empty();
    }

    @GetMapping("/top")
    public Page<PostMetadata> getTopFeed(Pageable pageable, @RequestParam(defaultValue = "DAY") TopPostTimeframe topPostTimeframe){
        return postService.getTopPostMetadataByTimeframe(pageable, topPostTimeframe);
    }

    @GetMapping("/latest")
    public Page<PostMetadata> getLatestFeed(Pageable pageable) {
        return postService.getLatestPostMetadata(pageable);
    }
}
