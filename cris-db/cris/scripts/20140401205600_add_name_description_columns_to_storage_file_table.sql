--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."storage_file" ADD COLUMN "name" varchar(255) NULL;
ALTER TABLE "public"."storage_file" ADD COLUMN "description" varchar(4000) NULL;


--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."storage_file" DROP COLUMN "description";
ALTER TABLE "public"."storage_file" DROP COLUMN "name";
