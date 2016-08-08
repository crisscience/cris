--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."tool" ADD COLUMN "uuid" uuid NOT NULL;
ALTER TABLE "public"."tool" ADD COLUMN "version_number" uuid NOT NULL;
ALTER TABLE "public"."tool" ADD COLUMN "key" varchar(250) NOT NULL;
ALTER TABLE "public"."tool" ADD COLUMN "content" varchar NULL;


--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."tool" DROP COLUMN "content";
ALTER TABLE "public"."tool" DROP COLUMN "key";
ALTER TABLE "public"."tool" DROP COLUMN "version_number";
ALTER TABLE "public"."tool" DROP COLUMN "uuid";
