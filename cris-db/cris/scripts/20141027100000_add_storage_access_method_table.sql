-- Migration SQL that makes the change goes here.
CREATE TABLE "public"."storage_access_method" (
    "id"                serial NOT NULL,
    "storage_id"        int4 NOT NULL,
    "type"              varchar(50) NOT NULL,
    "name"              varchar(250) NULL,
    "description"       varchar(4000) NULL,
    "uri"               varchar(4000) NOT NULL,
    "root"              varchar(4000) NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "storage_access_method_storage_id_index" on "public"."storage_access_method"("storage_id");

CREATE INDEX "storage_access_method_creator_id_index" on "public"."storage_access_method"("creator_id");
CREATE INDEX "storage_access_method_updater_id_index" on "public"."storage_access_method"("updater_id");
CREATE INDEX "storage_access_method_tenant_id_index" on "public"."storage_access_method"("tenant_id");

ALTER TABLE "public"."storage_access_method" ADD FOREIGN KEY("storage_id") REFERENCES "public"."storage"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."storage_access_method" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_access_method" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_access_method" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;


--//@UNDO
-- SQL to undo the change goes here.
DROP TABLE IF EXISTS "public"."storage_access_method_aud";
DROP TABLE "public"."storage_access_method";
