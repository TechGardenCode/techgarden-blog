package gg.techgarden.blog.persistence.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import gg.techgarden.blog.cache.profile.Profile;
import gg.techgarden.blog.model.ReactionParentType;
import gg.techgarden.blog.model.ReactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID parentId;

    @Enumerated(EnumType.STRING)
    private ReactionParentType parentType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="user_id", referencedColumnName = "sub")
    private Profile user;

    private String content;

    @Enumerated(EnumType.STRING)
    private ReactionType type;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
