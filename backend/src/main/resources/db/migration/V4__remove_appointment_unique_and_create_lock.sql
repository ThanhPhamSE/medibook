ALTER TABLE appointments
ADD COLUMN active_slot_key VARCHAR(150)
GENERATED ALWAYS AS (
    CASE
        WHEN status IN ('PENDING','CONFIRMED')
        THEN CONCAT(
            doctor_id,
            '_',
            DATE_FORMAT(start_datetime,'%Y-%m-%d %H:%i:%s')
        )
        ELSE NULL
    END
) STORED;


ALTER TABLE appointments
ADD CONSTRAINT uq_active_doctor_slot
UNIQUE(active_slot_key);


CREATE INDEX idx_appt_doctor_time_status
ON appointments(
    doctor_id,
    start_datetime,
    status
);