-- Migration SQL that makes the change goes here.
DROP TABLE IF EXISTS "public"."template_aud";
DROP TABLE "public"."template" CASCADE;

ALTER TABLE "public"."tool" ADD FOREIGN KEY("template_id") REFERENCES "public"."term"("id") ON DELETE CASCADE ON UPDATE NO ACTION;


--//@UNDO
-- SQL to undo the change goes here.
CREATE TABLE "public"."template" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "uuid"              uuid NOT NULL,
    "version_number"    uuid NOT NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "email"             varchar(250) NULL,
    "key"               varchar(250) NULL,
    "content"           varchar NOT NULL,
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

CREATE INDEX "template_asset_type_id_index" on "public"."template"("asset_type_id");
CREATE INDEX "template_name_index" on "public"."template"("name");
CREATE INDEX "template_image_id_index" on "public"."template"("image_id");
CREATE INDEX "template_owner_id_index" on "public"."template"("owner_id");
CREATE INDEX "template_status_id_index" on "public"."template"("status_id");

CREATE INDEX "template_creator_id_index" on "public"."template"("creator_id");
CREATE INDEX "template_updater_id_index" on "public"."template"("updater_id");
CREATE INDEX "template_tenant_id_index" on "public"."template"("tenant_id");

ALTER TABLE "public"."template" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."tool" ADD FOREIGN KEY("template_id") REFERENCES "public"."template"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
