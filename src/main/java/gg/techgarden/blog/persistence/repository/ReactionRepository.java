package gg.techgarden.blog.persistence.repository;

import gg.techgarden.blog.model.ReactionType;
import gg.techgarden.blog.persistence.entity.Reaction;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    @Query(
            value = """
            SELECT r.type, COUNT(*)
            FROM reaction r
            WHERE r.parent_id = :postId
            GROUP BY r.type
            """,
            nativeQuery = true
    )
    List<Tuple> findReactionCountByPostId(UUID postId);

    @Query(
            value = """
            SELECT DISTINCT(r.type)
            FROM reaction r
            WHERE r.parent_id = :postId
            AND r.user_id = :userId
            """,
            nativeQuery = true
    )
    List<String> findReactionCountByPostIdAndUserId(UUID postId, UUID userId);

    boolean existsByParentIdAndType(UUID parentId, ReactionType type);

    boolean existsByParentIdAndUserSubAndType(UUID parentId, UUID userId, ReactionType type);

    void removeByParentIdAndUserSubAndType(UUID parentId, UUID userId, ReactionType type);
}
