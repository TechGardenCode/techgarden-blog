ALTER TABLE post_metadata
    ADD public_post BOOLEAN;

ALTER TABLE post_metadata
    ALTER COLUMN public_post SET NOT NULL;

ALTER TABLE post_metadata
    DROP COLUMN is_public;