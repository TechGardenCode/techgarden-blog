ALTER TABLE post_body_json
    DROP CONSTRAINT fk_postbodyjson_on_post;

CREATE TABLE post_reaction
(
    id            UUID NOT NULL,
    post_id       UUID,
    user_id       UUID,
    reaction_type VARCHAR(255),
    CONSTRAINT pk_postreaction PRIMARY KEY (id)
);

ALTER TABLE post_reaction
    ADD CONSTRAINT FK_POSTREACTION_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

DROP TABLE post_body_json CASCADE;