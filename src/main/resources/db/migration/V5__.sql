ALTER TABLE post_reaction
    DROP CONSTRAINT fk_postreaction_on_post;

CREATE TABLE reaction
(
    id          UUID NOT NULL,
    parent_id   UUID,
    parent_type VARCHAR(255),
    user_id     UUID,
    content     VARCHAR(255),
    type        VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_reaction PRIMARY KEY (id)
);

ALTER TABLE reaction
    ADD CONSTRAINT FK_REACTION_ON_USER FOREIGN KEY (user_id) REFERENCES profile (sub);

DROP TABLE post_reaction CASCADE;