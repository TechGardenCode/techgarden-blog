package gg.techgarden.blog.service;

import gg.techgarden.blog.cache.profile.Profile;
import gg.techgarden.blog.cache.profile.ProfileRepository;
import gg.techgarden.blog.model.TopPostTimeframe;
import gg.techgarden.blog.persistence.entity.Post;
import gg.techgarden.blog.persistence.entity.PostMetadata;
import gg.techgarden.blog.persistence.repository.PostMetadataRepository;
import gg.techgarden.blog.persistence.repository.PostRepository;
import gg.techgarden.blog.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostMetadataRepository postMetadataRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public Page<PostMetadata> getAllPostMetadata(Pageable pageable) {
        return postMetadataRepository.findAllByPublicPostIsTrue(pageable);
    }

    @Transactional
    public Page<PostMetadata> getAllPostMetadataForCurrentUser(Pageable pageable) {
        UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
        try {
            if (!profileRepository.existsById(sub)) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
            }
            Profile profile = profileRepository.findById(sub).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
            return postMetadataRepository.findAllByAuthor(profile, pageable);
        } catch (HttpClientErrorException ex) {
            return Page.empty(pageable);
        }
    }

    public Page<PostMetadata> getLatestPostMetadata(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        return postMetadataRepository.findAllByPublicPostIsTrue(sortedPageable);
    }

    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional
    public Post createPost(Post post) {
        post.setId(null);
        post.getMetadata().setId(null);
        post.getBody().setId(null);
        return savePost(post);
    }

    @Transactional
    public Post updatePost(Post post) {
        if (getPostById(post.getId()) == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        return savePost(post);
    }

    public Post deletePost(UUID id) {
        UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
        Post post = getPostById(id);
        if (post == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        if (!post.getMetadata().getAuthor().getSub().equals(sub)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
        }
        postRepository.delete(post);
        return post;
    }

    public Post savePost(Post post) {
        post.getMetadata().setPost(post);
        post.getBody().setPost(post);
        setOrUpdateProfile(post.getMetadata());
        return postRepository.save(post);
    }

    void setOrUpdateProfile(PostMetadata postMetadata) {
        if (postMetadata == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Post metadata is required");
        }
        UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
        if (!profileRepository.existsById(sub)) {
            Profile profile = SecurityUtil.getCurrentUserProfile().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
            profileRepository.save(profile);
        }
        postMetadata.setAuthor(profileRepository.findById(sub).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND)));
    }

    public Post getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
        if (!post.getMetadata().isPublicPost()) {
            UUID sub = SecurityUtil.getCurrentUserSub().orElseThrow(() -> new HttpClientErrorException(HttpStatus.FORBIDDEN));
            if (!post.getMetadata().getAuthor().getSub().equals(sub)) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN);
            }
        }
        return post;
    }

    public Page<PostMetadata> getTopPostMetadataByTimeframe(Pageable pageable, TopPostTimeframe topPostTimeframe) {
        Instant since = switch (topPostTimeframe) {
            case DAY -> Instant.now().minus(1, ChronoUnit.DAYS);
            case WEEK -> Instant.now().minus(7, ChronoUnit.DAYS);
            case MONTH -> Instant.now().minus(30, ChronoUnit.DAYS);
            case YEAR -> Instant.now().minus(365, ChronoUnit.DAYS);
            case ALL_TIME -> null;
        };
        log.info("{}", since);
        return postMetadataRepository.findTopLikedSinceNative(since, pageable);
    }
}
