package gg.techgarden.blog.persistence.repository;

import gg.techgarden.blog.persistence.entity.PostBodyJson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostBodyJsonRepository extends JpaRepository<PostBodyJson, UUID> {
    void deleteAllByPostId(UUID postId);
}
