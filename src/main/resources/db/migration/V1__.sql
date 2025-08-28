CREATE TABLE post_metadata
(
    id          UUID NOT NULL,
    title       VARCHAR(255),
    description VARCHAR(255),
    author      VARCHAR(255),
    tags        TEXT[],
    categories  TEXT[],
    image_url   VARCHAR(255),
    created_at  TIMESTAMP WITHOUT TIME ZONE,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_postmetadata PRIMARY KEY (id)
);