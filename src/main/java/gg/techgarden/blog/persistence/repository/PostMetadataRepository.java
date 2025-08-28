package gg.techgarden.blog.persistence.repository;

import gg.techgarden.blog.persistence.entity.PostMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostMetadataRepository extends JpaRepository<PostMetadata, UUID> {
}
