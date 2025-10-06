package gg.techgarden.blog.persistence.repository;

import gg.techgarden.blog.persistence.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query(
            value = """
      SELECT p.*
      FROM posts p
      JOIN post_metadata pm ON pm.post_id = p.id
      LEFT JOIN likes l ON l.post_id = p.id
      WHERE pm.public_post = true
      GROUP BY p.id
      ORDER BY
        COUNT(*) FILTER (WHERE :since IS NULL OR l.created_at >= :since) DESC,
        p.created_at DESC,
        p.id DESC
      """,
            countQuery = "SELECT COUNT(*) FROM posts p JOIN post_metadata pm ON pm.post_id = p.id WHERE pm.public_post = true",
            nativeQuery = true
    )
    Page<Post> findTopLikedSinceNative(@Param("since") Instant since, Pageable pageable);
}
