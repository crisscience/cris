-- Migration SQL that makes the change goes here.
CREATE TABLE "public"."shortcut" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "uuid"              uuid UNIQUE NOT NULL,
    "version_number"    uuid NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "url"               varchar(500) NOT NULL,
    "parameters"        varchar(1500) NULL, -- use template definition? {a : {type: "boolean/number(with range check)/string(with regexp)/datetime", readOnly: true, defaultValue: "aaa", visibleToUser: true}, b: {...}}
    "http_method"       varchar(10) NULL DEFAULT 'GET',
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

CREATE INDEX "shortcut_name_index" on "public"."shortcut"("name");
CREATE INDEX "shortcut_image_id_index" on "public"."shortcut"("image_id");
CREATE INDEX "shortcut_owner_id_index" on "public"."shortcut"("owner_id");
CREATE INDEX "shortcut_status_id_index" on "public"."shortcut"("status_id");

CREATE INDEX "shortcut_uuid_index" on "public"."shortcut"("uuid");

CREATE INDEX "shortcut_creator_id_index" on "public"."shortcut"("creator_id");
CREATE INDEX "shortcut_updater_id_index" on "public"."shortcut"("updater_id");
CREATE INDEX "shortcut_tenant_id_index" on "public"."shortcut"("tenant_id");

ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;


--//@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS "public"."shortcut_aud";
DROP TABLE "public"."shortcut";
