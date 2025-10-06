package gg.techgarden.blog.persistence.repository;

import gg.techgarden.blog.cache.profile.Profile;
import gg.techgarden.blog.persistence.entity.Post;
import gg.techgarden.blog.persistence.entity.PostMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface PostMetadataRepository extends JpaRepository<PostMetadata, UUID> {
    Page<PostMetadata> findAllByPublicPostIsTrue(Pageable pageable);
    Page<PostMetadata> findAllByAuthor(Profile profile, Pageable pageable);
    @Query(
            value = """
      SELECT pm.*
      FROM post_metadata pm
      LEFT JOIN post_reaction pr ON pr.post_id = pm.post_id
      WHERE pm.public_post = true
      GROUP BY pm.created_at, pm.post_id
      ORDER BY
        COUNT(*) FILTER (WHERE cast(:since as timestamp) IS NULL OR pm.created_at >= cast(:since as timestamp)) DESC,
        pm.created_at DESC,
        pm.post_id DESC
      """,
            countQuery = "SELECT COUNT(*) FROM post_metadata pm WHERE pm.public_post = true",
            nativeQuery = true
    )
    Page<PostMetadata> findTopLikedSinceNative(@Param("since") Instant since, Pageable pageable);
}
