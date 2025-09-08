CREATE TABLE profile
(
    sub          UUID NOT NULL,
    display_name VARCHAR(255),
    email        VARCHAR(255),
    avatar_url   VARCHAR(255),
    bio          VARCHAR(255),
    locale       VARCHAR(255),
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_profile PRIMARY KEY (sub)
);

ALTER TABLE post_metadata
    ADD author_id UUID;

ALTER TABLE post_metadata
    ADD is_public BOOLEAN;

ALTER TABLE post_metadata
    ALTER COLUMN is_public SET NOT NULL;

ALTER TABLE post_metadata
    ADD CONSTRAINT FK_POSTMETADATA_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES profile (sub);

ALTER TABLE post_metadata
DROP
COLUMN author;