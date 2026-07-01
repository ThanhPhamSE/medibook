ALTER TABLE doctor_working_patterns
ADD CONSTRAINT uq_work_pattern
UNIQUE (
    doctor_id,
    day_of_week,
    start_time,
    end_time
);