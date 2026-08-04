-- Seed default working patterns (Monday to Sunday, 08:00 to 17:00) for all doctors who don't have patterns set
INSERT INTO doctor_working_patterns (doctor_id, day_of_week, start_time, end_time, slot_duration, buffer_duration)
SELECT d.id, days.day, '08:00:00', '17:00:00', 30, 0
FROM doctors d
CROSS JOIN (
    SELECT 'MON' AS day UNION ALL
    SELECT 'TUE' UNION ALL
    SELECT 'WED' UNION ALL
    SELECT 'THU' UNION ALL
    SELECT 'FRI' UNION ALL
    SELECT 'SAT' UNION ALL
    SELECT 'SUN'
) days
WHERE NOT EXISTS (
    SELECT 1 FROM doctor_working_patterns dwp 
    WHERE dwp.doctor_id = d.id AND dwp.day_of_week = days.day AND dwp.deleted_at IS NULL
);
