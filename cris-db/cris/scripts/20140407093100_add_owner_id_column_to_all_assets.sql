--// add version number to workflow
-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."storage" ADD COLUMN "owner_id" int4 NULL;
ALTER TABLE "public"."storage_file" ADD COLUMN "owner_id" int4 NULL;
ALTER TABLE "public"."small_object" ADD COLUMN "owner_id" int4 NULL;
ALTER TABLE "public"."computational_node" ADD COLUMN "owner_id" int4 NULL;

CREATE INDEX "storage_owner_id_index" on "public"."storage"("owner_id");
CREATE INDEX "storage_file_owner_id_index" on "public"."storage_file"("owner_id");
CREATE INDEX "small_object_owner_id_index" on "public"."small_object"("owner_id");
CREATE INDEX "computational_node_owner_id_index" on "public"."computational_node"("owner_id");

ALTER TABLE "public"."storage" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."small_object" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."computational_node" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;


--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."storage" DROP COLUMN "owner_id";
ALTER TABLE "public"."storage_file" DROP COLUMN "owner_id";
ALTER TABLE "public"."small_object" DROP COLUMN "owner_id";
ALTER TABLE "public"."computational_node" DROP COLUMN "owner_id";
