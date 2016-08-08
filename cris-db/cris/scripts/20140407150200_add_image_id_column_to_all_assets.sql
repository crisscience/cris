--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."storage" ADD COLUMN "image_id" int4 NULL;
ALTER TABLE "public"."storage_file" ADD COLUMN "image_id" int4 NULL;
ALTER TABLE "public"."small_object" ADD COLUMN "image_id" int4 NULL;
ALTER TABLE "public"."computational_node" ADD COLUMN "image_id" int4 NULL;

CREATE INDEX "storage_image_id_index" on "public"."storage"("image_id");
CREATE INDEX "storage_file_image_id_index" on "public"."storage_file"("image_id");
CREATE INDEX "small_object_image_id_index" on "public"."small_object"("image_id");
CREATE INDEX "computational_node_image_id_index" on "public"."computational_node"("image_id");

ALTER TABLE "public"."storage" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."small_object" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."computational_node" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."tool" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."term" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."report" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."resource" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."workflow" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_image_id_fkey";
ALTER TABLE "public"."term" DROP CONSTRAINT "term_image_id_fkey";
ALTER TABLE "public"."report" DROP CONSTRAINT "report_image_id_fkey";
ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_image_id_fkey";
ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_image_id_fkey";
ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_image_id_fkey";

ALTER TABLE "public"."storage" DROP COLUMN "image_id";
ALTER TABLE "public"."storage_file" DROP COLUMN "image_id";
ALTER TABLE "public"."small_object" DROP COLUMN "image_id";
ALTER TABLE "public"."computational_node" DROP COLUMN "image_id";
