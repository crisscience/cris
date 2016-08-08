-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."job" DROP CONSTRAINT "job_status_id_fkey";

DROP TABLE IF EXISTS "public"."enum_job_status_aud";
DROP TABLE "public"."enum_job_status";


--//@UNDO
-- SQL to undo the change goes here.
-- Add enum job status table
CREATE TABLE "public"."enum_job_status" (
    "id"                int4 NOT NULL,
    "code"              int4 NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    PRIMARY KEY("id")
);

INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(1, 1, 'Created', 'The job is created by a user');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(2, 2, 'Submitted', 'The job is submitted by a user');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(3, 3, 'Started', 'The job is started');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(4, 4, 'Finished', 'The job is finished');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(5, 5, 'Cancelled', 'The job is cancelled by a user');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(6, 6, 'Close', 'The job is closed by a user');

-- Add foreign keys
ALTER TABLE "public"."job" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_job_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
