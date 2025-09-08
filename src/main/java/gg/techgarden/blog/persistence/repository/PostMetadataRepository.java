package gg.techgarden.blog.persistence.repository;

import gg.techgarden.blog.cache.profile.Profile;
import gg.techgarden.blog.persistence.entity.PostMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostMetadataRepository extends JpaRepository<PostMetadata, UUID> {
    Page<PostMetadata> findAllByPublicPostIsTrue(Pageable pageable);
    Page<PostMetadata> findAllByAuthor(Profile profile, Pageable pageable);
}
