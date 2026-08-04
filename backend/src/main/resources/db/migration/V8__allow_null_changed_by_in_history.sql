-- Allow changed_by to be NULL in appointment_status_histories
-- This supports system-triggered transitions (e.g. payment confirmation via PayOS)
-- where there is no logged-in user performing the action.
ALTER TABLE appointment_status_histories
    MODIFY COLUMN changed_by BIGINT NULL;
