--// add project_id, experiment_id and job_id to storage_file
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."storage_file" ADD COLUMN "project_id" int4 NULL;
ALTER TABLE "public"."storage_file" ADD COLUMN "experiment_id" int4 NULL;
ALTER TABLE "public"."storage_file" ADD COLUMN "job_id" int4 NULL;

CREATE INDEX "storage_file_project_id_index" on "public"."storage_file"("project_id");
CREATE INDEX "storage_file_experiment_id_index" on "public"."storage_file"("experiment_id");
CREATE INDEX "storage_file_job_id_index" on "public"."storage_file"("job_id");

ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("project_id") REFERENCES "public"."project"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("experiment_id") REFERENCES "public"."experiment"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("job_id") REFERENCES "public"."job"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."storage_file" DROP COLUMN "job_id";
ALTER TABLE "public"."storage_file" DROP COLUMN "experiment_id";
ALTER TABLE "public"."storage_file" DROP COLUMN "project_id";
