package gg.techgarden.blog.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReactionType {
    LIKE("LIKE"),
    UNICORN("UNICORN"),
    MIND_BLOWN("MIND_BLOWN"),
    PARTY("PARTY"),
    FIRE("FIRE"),
    COMMENT("COMMENT");

    private final String value;
}
