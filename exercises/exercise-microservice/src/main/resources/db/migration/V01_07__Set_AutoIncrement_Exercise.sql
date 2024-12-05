DO $$
    BEGIN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'exercise') THEN
            PERFORM setval(pg_get_serial_sequence('exercise', 'id'), 25);
        END IF;
    END $$;