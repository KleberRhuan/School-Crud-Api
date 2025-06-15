CREATE TABLE IF NOT EXISTS account.refresh_tokens (
                                                      series      UUID            PRIMARY KEY,
                                                      user_id     BIGINT          NOT NULL,
                                                      expires_at  TIMESTAMPTZ     NOT NULL,
                                                      used        BOOLEAN         NOT NULL DEFAULT false,
                                                      created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    
    constraint fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES account.users(id)
            ON DELETE CASCADE
);


CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user
    ON account.refresh_tokens (user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires
    ON account.refresh_tokens (expires_at);