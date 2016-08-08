-- Migration SQL that makes the change goes here.
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_asset_type_id_fkey";

ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_status_id_fkey";
ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_asset_type_id_fkey";

ALTER TABLE "public"."template" DROP CONSTRAINT "template_status_id_fkey";
ALTER TABLE "public"."template" DROP CONSTRAINT "template_asset_type_id_fkey";

ALTER TABLE "public"."term" DROP CONSTRAINT "term_status_id_fkey";
ALTER TABLE "public"."term" DROP CONSTRAINT "term_asset_type_id_fkey";

ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_status_id_fkey";
ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_asset_type_id_fkey";

ALTER TABLE "public"."report" DROP CONSTRAINT "report_status_id_fkey";
ALTER TABLE "public"."report" DROP CONSTRAINT "report_asset_type_id_fkey";

ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_status_id_fkey";
ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_asset_type_id_fkey";

ALTER TABLE "public"."project" DROP CONSTRAINT "project_status_id_fkey";
ALTER TABLE "public"."project" DROP CONSTRAINT "project_asset_type_id_fkey";

ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_status_id_fkey";
ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_asset_type_id_fkey";

ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_status_id_fkey";
ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_asset_type_id_fkey";

ALTER TABLE "public"."tile" DROP CONSTRAINT "tile_status_id_fkey";
ALTER TABLE "public"."tile" DROP CONSTRAINT "tile_asset_type_id_fkey";

ALTER TABLE "public"."shortcut" DROP CONSTRAINT "shortcut_status_id_fkey";
ALTER TABLE "public"."shortcut" DROP CONSTRAINT "shortcut_asset_type_id_fkey";

--ALTER TABLE "public"."small_object" DROP CONSTRAINT "small_object_status_id_fkey";
--ALTER TABLE "public"."small_object" DROP CONSTRAINT "small_object_asset_type_id_fkey";

--ALTER TABLE "public"."computational_node" DROP CONSTRAINT "computational_node_status_id_fkey";
--ALTER TABLE "public"."computational_node" DROP CONSTRAINT "computational_node_asset_type_id_fkey";

--ALTER TABLE "public"."storage" DROP CONSTRAINT "storage_status_id_fkey";
--ALTER TABLE "public"."storage" DROP CONSTRAINT "storage_asset_type_id_fkey";

--ALTER TABLE "public"."storage_file" DROP CONSTRAINT "storage_file_status_id_fkey";
--ALTER TABLE "public"."storage_file" DROP CONSTRAINT "storage_file_asset_type_id_fkey";

DROP TABLE IF EXISTS "public"."enum_asset_type_aud";
DROP TABLE IF EXISTS "public"."enum_asset_status_aud";
DROP TABLE "public"."enum_asset_type";
DROP TABLE "public"."enum_asset_status";


--//@UNDO
-- SQL to undo the change goes here.
-- Add asset tables
CREATE TABLE "public"."enum_asset_status" (
    "id"                int4 NOT NULL,
    "code"              int4 NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    PRIMARY KEY("id")
);

INSERT INTO "public"."enum_asset_status"("id", "code", "name", "description") VALUES(1, 1, 'Available', 'This piece of asset is available for use');
INSERT INTO "public"."enum_asset_status"("id", "code", "name", "description") VALUES(2, 2, 'In Maintenance', 'This piece of asset is in maintenance and will be available');
INSERT INTO "public"."enum_asset_status"("id", "code", "name", "description") VALUES(3, 3, 'Retired', 'This piece of asset is not available');

CREATE TABLE "public"."enum_asset_type" (
    "id"                int4 NOT NULL,
    "code"              int4 NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    PRIMARY KEY("id")
);

INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(1, 1, '*Content', 'A content');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(2, 2, 'Tool', 'A tool');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(3, 3, '*Dataset', 'A dataset');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(4, 4, '*Service', 'A service');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(5, 5, 'Project', 'A project');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(7, 7, 'Experiment', 'A resource');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(6, 6, 'Resource', 'A resource');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(8, 8, 'Workflow', 'A workflow');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(9, 9, 'Vocabulary', 'A vocabulary');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(13, 13, 'Template', 'A template');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(10, 10, 'Storage', 'A storage');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(11, 11, 'Storage File', 'A storage file');
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(12, 12, 'Computational Node', 'A computational node');

-- Add foreign keys
ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."shortcut" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."tile" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."tile" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."resource" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."resource" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."experiment" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."experiment" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."project" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."project" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."workflow" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."workflow" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."report" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."report" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."tool" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."tool" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."term" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."term" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."template" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE "public"."permission" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
