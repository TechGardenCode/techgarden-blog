package gg.techgarden.blog.persistence.repository;

import gg.techgarden.blog.persistence.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
}
