-- Migration SQL that makes the change goes here.
CREATE TABLE "public"."storage_path" (
    "id"                serial NOT NULL,
    "ancestor_type"     varchar(100),
    "ancestor_id"       int4,
    "descendant_type"   varchar(100),
    "descendant_id"     int4,
    "depth"             int4,

    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "storage_path_ancestor_id_index" on "public"."storage_path"("ancestor_id");
CREATE INDEX "storage_path_descendant_id_index" on "public"."storage_path"("descendant_id");

CREATE INDEX "storage_path_creator_id_index" on "public"."storage_path"("creator_id");
CREATE INDEX "storage_path_updater_id_index" on "public"."storage_path"("updater_id");
CREATE INDEX "storage_path_tenant_id_index" on "public"."storage_path"("tenant_id");

ALTER TABLE "public"."storage_path" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_path" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_path" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;


--//@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS "public"."storage_path_aud";
DROP TABLE "public"."storage_path";
