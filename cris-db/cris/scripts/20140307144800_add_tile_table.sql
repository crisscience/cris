-- Migration SQL that makes the change goes here.
CREATE TABLE "public"."tile" (
    "id"                serial NOT NULL,
    "parent_id"         int4 NULL,
    "asset_type_id"     int4 NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "location"          varchar(4000) NULL,
    "html"              varchar(4000) NULL,
    "style"             varchar(4000) NULL,
    "shortcut_id"       int4 NULL,
    "image_id"          int4 NULL,
    "owner_id"          int4 NULL,
    "status_id"         int4 NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "tile_name_index" on "public"."tile"("name");
CREATE INDEX "tile_image_id_index" on "public"."tile"("image_id");
CREATE INDEX "tile_owner_id_index" on "public"."tile"("owner_id");
CREATE INDEX "tile_status_id_index" on "public"."tile"("status_id");

CREATE INDEX "tile_parent_id_index" on "public"."tile"("parent_id");

CREATE INDEX "tile_creator_id_index" on "public"."tile"("creator_id");
CREATE INDEX "tile_updater_id_index" on "public"."tile"("updater_id");
CREATE INDEX "tile_tenant_id_index" on "public"."tile"("tenant_id");

ALTER TABLE "public"."tile" ADD FOREIGN KEY("parent_id") REFERENCES "public"."tile"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."tile" ADD FOREIGN KEY("shortcut_id") REFERENCES "public"."shortcut"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."tile" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."tile" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."tile" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."tile" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."tile" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."tile" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."tile" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;


--//@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS "public"."tile_aud";
DROP TABLE "public"."tile";
