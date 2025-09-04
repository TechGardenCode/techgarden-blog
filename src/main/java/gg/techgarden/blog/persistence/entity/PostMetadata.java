package gg.techgarden.blog.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMetadata {
    @Id
    @Column(name = "post_id")
    private UUID id;

    @OneToOne
    @MapsId
    @JsonIgnore
    @JoinColumn(name="post_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Post post;

    private String title;
    private String description;
    private String author;
    private List<String> tags;
    private List<String> categories;
    private String imageUrl;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
