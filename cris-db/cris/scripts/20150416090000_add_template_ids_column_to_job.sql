--// add template IDs to job
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."job" ADD COLUMN "template_uuids" varchar;


--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."job" DROP COLUMN "template_uuids";
