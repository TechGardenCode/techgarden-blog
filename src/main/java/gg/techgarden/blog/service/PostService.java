package gg.techgarden.blog.service;

import gg.techgarden.blog.persistence.entity.Post;
import gg.techgarden.blog.persistence.entity.PostMetadata;
import gg.techgarden.blog.persistence.repository.PostMetadataRepository;
import gg.techgarden.blog.persistence.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMetadataRepository postMetadataRepository;
    private final PostRepository postRepository;

    public Page<PostMetadata> getAllPostMetadata(Pageable pageable) {
        return postMetadataRepository.findAll(pageable);
    }

    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post createPost(Post post) {
        post.setId(null);
        post.getMetadata().setId(null);
        post.getMetadata().setPost(post);
        post.getBody().setId(null);
        post.getBody().setPost(post);
        return savePost(post);
    }

    public Post updatePost(Post post) {
        if (getPostById(post.getId()) == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
        post.getMetadata().setPost(post);
        post.getBody().setPost(post);
        return savePost(post);
    }

    public Post savePost(Post post) {
        updatePostMetadata(post);
        return postRepository.save(post);
    }

    private void updatePostMetadata(Post post) {
        post.getMetadata().setDescription(post.getBody().getContent().substring(0, 255));
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }
}
