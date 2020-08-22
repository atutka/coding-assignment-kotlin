CREATE TABLE tracker.project (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,

    CONSTRAINT start_and_end_date_checker CHECK ( start_date < end_date)
);