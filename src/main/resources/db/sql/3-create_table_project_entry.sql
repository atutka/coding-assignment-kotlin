CREATE TABLE tracker.project_entry (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    time_spent NUMERIC(4, 2) NOT NULL,
    description TEXT,
    project_id BIGINT NOT NULL,

    CONSTRAINT fk_entry_project_id FOREIGN KEY (project_id) REFERENCES tracker.project (id),
    CONSTRAINT time_spend_checker CHECK (time_spent <= 10 AND time_spent > 0)
);