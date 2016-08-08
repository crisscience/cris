--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."group" ADD COLUMN "enabled" bool NOT NULL DEFAULT true;

--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."group" DROP COLUMN "enabled";
