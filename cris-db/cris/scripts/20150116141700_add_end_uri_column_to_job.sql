--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."job" ADD COLUMN "end_uri" varchar;


--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."job" DROP COLUMN "end_uri";
