-- Migration SQL that makes the change goes here.
CREATE TABLE "public"."term" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "vocabulary_uuid"   uuid NULL,
    "vocabulary_id"     int4 NULL,
    "is_template"       boolean NULL,
    "uuid"              uuid NOT NULL,
    "version_number"    uuid NOT NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "type"              varchar(250) NULL,
    "unit"              varchar(250) NULL,
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

CREATE INDEX "term_asset_type_id_index" on "public"."term"("asset_type_id");
CREATE INDEX "term_name_index" on "public"."term"("name");
CREATE INDEX "term_image_id_index" on "public"."term"("image_id");
CREATE INDEX "term_owner_id_index" on "public"."term"("owner_id");
CREATE INDEX "term_status_id_index" on "public"."term"("status_id");

CREATE INDEX "term_creator_id_index" on "public"."term"("creator_id");
CREATE INDEX "term_updater_id_index" on "public"."term"("updater_id");
CREATE INDEX "term_tenant_id_index" on "public"."term"("tenant_id");

CREATE INDEX "term_vocabulary_uuid_index" on "public"."term"("vocabulary_uuid");
CREATE INDEX "term_vocabulary_id_index" on "public"."term"("vocabulary_id");

ALTER TABLE "public"."term" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."term" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."term" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."term" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."term" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."term" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."term" ADD FOREIGN KEY("vocabulary_id") REFERENCES "public"."vocabulary"("id") ON DELETE CASCADE ON UPDATE NO ACTION;


--//@UNDO
-- SQL to undo the change goes here.
DROP TABLE "public"."term";
