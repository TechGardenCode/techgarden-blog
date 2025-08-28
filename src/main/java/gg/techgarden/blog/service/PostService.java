package gg.techgarden.blog.service;

import gg.techgarden.blog.persistence.entity.PostMetadata;
import gg.techgarden.blog.persistence.repository.PostMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostMetadataRepository postMetadataRepository;

    public Page<PostMetadata> getAllPostMetadata(Pageable pageable) {
        return postMetadataRepository.findAll(pageable);
    }
}
