package gg.techgarden.blog.controller;

import gg.techgarden.blog.persistence.entity.Post;
import gg.techgarden.blog.persistence.entity.PostMetadata;
import gg.techgarden.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/metadata")
    public Page<PostMetadata> getAllPostMetadata(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.getAllPostMetadata(pageable);
    }

    @GetMapping
    public Page<Post> getPosts(@PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return postService.getPosts(pageable);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        if (post.getMetadata() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Post metadata is required");
        }
        if (post.getBody() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Post body is required");
        }
        return postService.createPost(post);
    }


    @GetMapping("/{id}")
    public Post getPostById(@PathVariable UUID id) {
        return postService.getPostById(id);
    }

    @PutMapping("/{id}")
    public Post updatePost(@PathVariable UUID id, @RequestBody Post post) {
        if (post.getId() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Post ID is required for update");
        }
        if (post.getMetadata() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Post metadata is required");
        }
        if (post.getBody() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Post body is required");
        }
        if (!id.equals(post.getId())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Path ID and Post ID do not match");
        }
        return postService.updatePost(post);
    }
}
