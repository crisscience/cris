-- fix audit table colum size
DO
$$
DECLARE
   r RECORD;
   s RECORD;
BEGIN
    FOR r IN
        SELECT * FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' AND table_name LIKE '%_aud' ORDER BY table_name
    LOOP
        -- for each audit table
        RAISE INFO 'Table: %', r.table_name;
        FOR s IN
            SELECT DISTINCT t_aud.table_catalog, t_aud.table_schema, t_aud.table_name, t_aud.column_name, t_aud.ordinal_position, t_aud.data_type, t_aud.character_maximum_length, t.character_maximum_length as character_maximum_length_orig
            FROM information_schema.columns t_aud LEFT JOIN information_schema.columns t
            ON t_aud.table_name = t.table_name || '_aud' AND t_aud.column_name = t.column_name
            WHERE t_aud.table_name = r.table_name AND t_aud.data_type = 'character varying'
            ORDER BY t_aud.ordinal_position
        LOOP
            -- for each column that needs to be fixed
            IF s.character_maximum_length_orig IS NULL AND s.character_maximum_length IS NOT NULL THEN
                RAISE INFO 'Column: %', s;
                EXECUTE 'ALTER TABLE ' || r.table_name || ' ALTER COLUMN ' || s.column_name || ' TYPE VARCHAR';
            ELSEIF s.character_maximum_length_orig IS NOT NULL AND s.character_maximum_length_orig <> s.character_maximum_length THEN
                RAISE INFO 'Column: %', s;
                EXECUTE 'ALTER TABLE ' || r.table_name || ' ALTER COLUMN ' || s.column_name || ' TYPE VARCHAR(' || s.character_maximum_length_orig ||')';
            END IF;
        END LOOP;
    END LOOP;
END;
$$;
