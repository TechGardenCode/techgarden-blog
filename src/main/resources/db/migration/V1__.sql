CREATE TABLE post
(
    id         UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_post PRIMARY KEY (id)
);

CREATE TABLE post_body
(
    post_id    UUID NOT NULL,
    content    OID,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_postbody PRIMARY KEY (post_id)
);

CREATE TABLE post_body_json
(
    id          UUID    NOT NULL,
    line_number INTEGER NOT NULL,
    post_id     UUID    NOT NULL,
    type        VARCHAR(255),
    subtype     VARCHAR(255),
    text        VARCHAR(65535),
    CONSTRAINT pk_postbodyjson PRIMARY KEY (id)
);

CREATE TABLE post_metadata
(
    post_id     UUID NOT NULL,
    title       VARCHAR(255),
    description VARCHAR(255),
    author      VARCHAR(255),
    tags        TEXT[],
    categories  TEXT[],
    image_url   VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_postmetadata PRIMARY KEY (post_id)
);

ALTER TABLE post_body_json
    ADD CONSTRAINT FK_POSTBODYJSON_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE post_body
    ADD CONSTRAINT FK_POSTBODY_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE post_metadata
    ADD CONSTRAINT FK_POSTMETADATA_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);