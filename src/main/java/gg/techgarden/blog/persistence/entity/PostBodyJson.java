package gg.techgarden.blog.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gg.techgarden.blog.model.PostBodyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostBodyJson {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int lineNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Enumerated(EnumType.STRING)
    private PostBodyType type;
    private String subtype;
    @Column(length = 65535)
    private String text;
}
