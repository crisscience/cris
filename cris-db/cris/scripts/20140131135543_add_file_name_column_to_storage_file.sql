--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."storage_file" ADD COLUMN "file_name" varchar(256);


--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."storage_file" DROP COLUMN "file_name";
