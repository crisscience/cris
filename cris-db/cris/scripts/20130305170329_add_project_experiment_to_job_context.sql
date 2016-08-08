--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."job_context" ADD COLUMN "project_id" integer NULL;
ALTER TABLE "public"."job_context" ADD COLUMN "experiment_id" integer NULL;

CREATE INDEX "job_context_project_id_index" on "public"."job_context"("project_id");
CREATE INDEX "job_context_experiment_id_index" on "public"."job_context"("experiment_id");

ALTER TABLE "public"."job_context" ADD FOREIGN KEY("project_id") REFERENCES "public"."project"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."job_context" ADD FOREIGN KEY("experiment_id") REFERENCES "public"."experiment"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."job_context" ALTER COLUMN job_id DROP NOT NULL;

--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."job_context" ALTER COLUMN job_id SET NOT NULL;

ALTER TABLE "public"."job_context" DROP COLUMN "project_id";
ALTER TABLE "public"."job_context" DROP COLUMN "experiment_id";
