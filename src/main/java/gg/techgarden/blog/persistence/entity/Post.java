package gg.techgarden.blog.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private PostMetadata metadata;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private PostBody body;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostBodyJson> postBodyJson;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
