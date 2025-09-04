package gg.techgarden.blog.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PostBodyType {
    HEADING("heading"),
    PARAGRAPH("paragraph"),
    EMBED("embed"),
    QUOTE("quote"),
    CODE_BLOCK("code_block"),
    DIVIDER("divider"),
    ORDERED_LIST("ordered_list"),
    UNORDERED_LIST("unordered_list"),;

    private final String value;
}
