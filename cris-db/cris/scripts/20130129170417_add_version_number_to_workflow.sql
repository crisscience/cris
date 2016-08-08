--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."workflow" ADD COLUMN "version_number" integer NULL;
UPDATE "public"."workflow" SET version_number = 0;
ALTER TABLE "public"."workflow" ALTER COLUMN "version_number" SET NOT NULL;


--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."workflow" DROP COLUMN "version_number";
