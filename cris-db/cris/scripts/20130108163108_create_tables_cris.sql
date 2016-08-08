--// create tables cris
-- Migration SQL that makes the change goes here.
-- Create tables and indexes
CREATE TABLE "public"."classification" (
    "id"                serial NOT NULL,
    "code"              varchar(100) NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    UNIQUE("code", "tenant_id"),
    UNIQUE("name", "tenant_id"),
    PRIMARY KEY("id")
);

CREATE INDEX "classification_name_index" on "public"."classification"("name");
CREATE INDEX "classification_code_index" on "public"."classification"("code");

CREATE INDEX "classification_creator_id_index" on "public"."classification"("creator_id");
CREATE INDEX "classification_updater_id_index" on "public"."classification"("updater_id");
CREATE INDEX "classification_tenant_id_index" on "public"."classification"("tenant_id");

CREATE TABLE "public"."computational_node" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "name"              varchar(256) NOT NULL,
    "description"       varchar(4000) NULL,
    "type"              varchar(256) NOT NULL,
    "ip_address"        varchar(256) NOT NULL,
    "location"          varchar(256) NULL,
    "capacity"          varchar(256) NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "computational_node_asset_type_id_index" on "public"."computational_node"("asset_type_id");
CREATE INDEX "computational_node_name_index" on "public"."computational_node"("name");

CREATE INDEX "computational_node_creator_id_index" on "public"."computational_node"("creator_id");
CREATE INDEX "computational_node_updater_id_index" on "public"."computational_node"("updater_id");
CREATE INDEX "computational_node_tenant_id_index" on "public"."computational_node"("tenant_id");

CREATE TABLE "public"."configuration"  (
    "id"                serial NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    "type"              varchar(100) NOT NULL,
    "value_text"        varchar NULL,
    "value_integer"     int4 NULL,
    "value_bool"        bool NULL,
    "value_binary"      bytea NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "configuration_name_index" on "public"."configuration"("name");

CREATE INDEX "configuration_creator_id_index" on "public"."configuration"("creator_id");
CREATE INDEX "configuration_updater_id_index" on "public"."configuration"("updater_id");
CREATE INDEX "configuration_tenant_id_index" on "public"."configuration"("tenant_id");

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
INSERT INTO "public"."enum_asset_type"("id", "code", "name", "description") VALUES(2, 2, 'Tool', 'A tool');

CREATE TABLE "public"."enum_job_status" (
    "id"                int4 NOT NULL,
    "code"              int4 NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    PRIMARY KEY("id")
);

INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(1, 1, 'Created', 'The job is created by a user');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(2, 2, 'Submitted', 'The job is submitted by a user');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(3, 3, 'Started', 'The job is started');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(4, 4, 'Finished', 'The job is finished');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(5, 5, 'Cancelled', 'The job is cancelled by a user');
INSERT INTO "public"."enum_job_status"("id", "code", "name", "description") VALUES(6, 6, 'Close', 'The job is closed by a user');

CREATE TABLE "public"."enum_operation" (
    "id"                int4 NOT NULL,
    "code"              int4 NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    PRIMARY KEY("id")
);

INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 1, 1, 'List', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 2, 2, 'Show', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 3, 3, 'Create', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 4, 4, 'Update', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 5, 5, 'Delete', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 6, 6, 'Import', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 7, 7, 'Download', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 8, 8, 'Run', 'for service consumer');
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES( 9, 9, 'Serve', 'for service provider');
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES(10, 10, 'Maintain', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES(11, 11, 'Retire', NULL);
INSERT INTO "public"."enum_operation"("id", "code", "name", "description") VALUES(12, 12, 'Avail', NULL);

CREATE TABLE "public"."enum_role_type" (
    "id"                int4 NOT NULL,
    "code"              int4 NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    PRIMARY KEY("id")
);

INSERT INTO "public"."enum_role_type"("id", "code", "name", "description") VALUES(1, 1, 'ROLE_ADMIN', NULL);
INSERT INTO "public"."enum_role_type"("id", "code", "name", "description") VALUES(2, 2, 'ROLE_MANAGER', NULL);
INSERT INTO "public"."enum_role_type"("id", "code", "name", "description") VALUES(3, 3, 'ROLE_STAFF', NULL);
INSERT INTO "public"."enum_role_type"("id", "code", "name", "description") VALUES(4, 4, 'ROLE_USER', NULL);

CREATE TABLE "public"."experiment" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "project_id"        int4 NOT NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "email"             varchar(250) NULL,
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

CREATE INDEX "experiment_asset_type_id_index" on "public"."experiment"("asset_type_id");
CREATE INDEX "experiment_project_id_index" on "public"."experiment"("project_id");
CREATE INDEX "experiment_name_index" on "public"."experiment"("name");
CREATE INDEX "experiment_image_id_index" on "public"."experiment"("image_id");
CREATE INDEX "experiment_owner_id_index" on "public"."experiment"("owner_id");
CREATE INDEX "experiment_status_id_index" on "public"."experiment"("status_id");

CREATE INDEX "experiment_creator_id_index" on "public"."experiment"("creator_id");
CREATE INDEX "experiment_updater_id_index" on "public"."experiment"("updater_id");
CREATE INDEX "experiment_tenant_id_index" on "public"."experiment"("tenant_id");

CREATE TABLE "public"."group" (
    "id"                serial NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    "email"             varchar(100) NULL,
    "image_id"          int4 NULL,
    "owner_id"          int4 NULL,
    "classification_id" int4 NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    UNIQUE("name", "tenant_id"),
    PRIMARY KEY("id")
);

CREATE INDEX "group_name_index" on "public"."group"("name");
CREATE INDEX "group_email_index" on "public"."group"("email");
CREATE INDEX "group_image_id_index" on "public"."group"("image_id");
CREATE INDEX "group_owner_id_index" on "public"."group"("owner_id");
CREATE INDEX "group_classification_id_index" on "public"."group"("classification_id");

CREATE INDEX "group_creator_id_index" on "public"."group"("creator_id");
CREATE INDEX "group_updater_id_index" on "public"."group"("updater_id");
CREATE INDEX "group_tenant_id_index" on "public"."group"("tenant_id");

CREATE TABLE "public"."group_user" (
    "id"                serial NOT NULL,
    "group_id"          int4 NOT NULL,
    "user_id"           int4 NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    UNIQUE("group_id", "user_id"),
    PRIMARY KEY("id")
);

CREATE INDEX "group_user_group_id_index" on "public"."group_user"("group_id");
CREATE INDEX "group_user_user_id_index" on "public"."group_user"("user_id");

CREATE INDEX "group_user_creator_id_index" on "public"."group_user"("creator_id");
CREATE INDEX "group_user_updater_id_index" on "public"."group_user"("updater_id");
CREATE INDEX "group_user_tenant_id_index" on "public"."group_user"("tenant_id");

CREATE TABLE "public"."job" (
    "id"                serial NOT NULL,
    "parent_id"         int4 NULL,
    "group_id"          int4 NULL,
    "user_id"           int4 NULL,
    "resource_id"       int4 NULL,
    "workflow_id"       int4 NULL,
    "project_id"        int4 NULL,
    "experiment_id"     int4 NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    "parameters"        varchar NULL,
    "status_id"         int4 NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp,
    "time_updated"      timestamp,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "job_parent_id_index" on "public"."job"("parent_id");
CREATE INDEX "job_group_id_index" on "public"."job"("group_id");
CREATE INDEX "job_user_id_index" on "public"."job"("user_id");
CREATE INDEX "job_resource_id_index" on "public"."job"("resource_id");
CREATE INDEX "job_workflow_id_index" on "public"."job"("workflow_id");
CREATE INDEX "job_project_id_index" on "public"."job"("project_id");
CREATE INDEX "job_experiment_id_index" on "public"."job"("experiment_id");
CREATE INDEX "job_name_index" on "public"."job"("name");

CREATE INDEX "job_creator_id_index" on "public"."job"("creator_id");
CREATE INDEX "job_updater_id_index" on "public"."job"("updater_id");
CREATE INDEX "job_tenant_id_index" on "public"."job"("tenant_id");

CREATE TABLE "public"."job_context" (
    "id"                serial NOT NULL,
    "job_id"            int4 NOT NULL,
    "task"              varchar(250) NULL,
    "term_uuid"         uuid NULL,
    "term_version"      uuid NULL,
    "name"              varchar(250) NULL,
    "value"             varchar NULL,
    "status"            varchar(50) NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "job_context_job_id_index" on "public"."job_context"("job_id");
CREATE INDEX "job_context_task_index" on "public"."job_context"("task");
CREATE INDEX "job_context_term_uuid_index" on "public"."job_context"("term_uuid");
CREATE INDEX "job_context_name_index" on "public"."job_context"("name");

CREATE INDEX "job_context_creator_id_index" on "public"."job_context"("creator_id");
CREATE INDEX "job_context_updater_id_index" on "public"."job_context"("updater_id");
CREATE INDEX "job_context_tenant_id_index" on "public"."job_context"("tenant_id");

CREATE TABLE "public"."permission" (
    "id"                serial NOT NULL,
    "group_id"          int4 NULL,
    "user_id"           int4 NULL,
    "role_type_id"      int4 NULL,
    "role_id"           int4 NULL,
    "operation_id"      int4 NULL,
    "asset_type_id"     int4 NULL,
    "asset_id"          int4 NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "permission_group_id_index" on "public"."permission"("group_id");
CREATE INDEX "permission_user_id_index" on "public"."permission"("user_id");
CREATE INDEX "permission_role_type_id_index" on "public"."permission"("role_type_id");
CREATE INDEX "permission_role_id_index" on "public"."permission"("role_id");
CREATE INDEX "permission_operation_id_index" on "public"."permission"("operation_id");
CREATE INDEX "permission_asset_type_id_index" on "public"."permission"("asset_type_id");
CREATE INDEX "permission_asset_id_index" on "public"."permission"("asset_id");

CREATE INDEX "permission_creator_id_index" on "public"."permission"("creator_id");
CREATE INDEX "permission_updater_id_index" on "public"."permission"("updater_id");
CREATE INDEX "permission_tenant_id_index" on "public"."permission"("tenant_id");

CREATE TABLE "public"."project" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "email"             varchar(250) NULL,
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

CREATE INDEX "project_asset_type_id_index" on "public"."project"("asset_type_id");
CREATE INDEX "project_name_index" on "public"."project"("name");
CREATE INDEX "project_image_id_index" on "public"."project"("image_id");
CREATE INDEX "project_owner_id_index" on "public"."project"("owner_id");
CREATE INDEX "project_status_id_index" on "public"."project"("status_id");

CREATE INDEX "project_creator_id_index" on "public"."project"("creator_id");
CREATE INDEX "project_updater_id_index" on "public"."project"("updater_id");
CREATE INDEX "project_tenant_id_index" on "public"."project"("tenant_id");

CREATE TABLE "public"."report" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "uuid"              uuid NOT NULL,
    "version_number"    int4 NOT NULL,
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

CREATE INDEX "report_asset_type_id_index" on "public"."report"("asset_type_id");
CREATE INDEX "report_name_index" on "public"."report"("name");
CREATE INDEX "report_image_id_index" on "public"."report"("image_id");
CREATE INDEX "report_owner_id_index" on "public"."report"("owner_id");
CREATE INDEX "report_status_id_index" on "public"."report"("status_id");

CREATE INDEX "report_creator_id_index" on "public"."report"("creator_id");
CREATE INDEX "report_updater_id_index" on "public"."report"("updater_id");
CREATE INDEX "report_tenant_id_index" on "public"."report"("tenant_id");

CREATE TABLE "public"."resource" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "email"             varchar(250) NULL,
    "physical_location_label" varchar(250) NULL,
    "physical_location"     varchar(250) NULL,
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

CREATE INDEX "resource_asset_type_id_index" on "public"."resource"("asset_type_id");
CREATE INDEX "resource_name_index" on "public"."resource"("name");
CREATE INDEX "resource_image_id_index" on "public"."resource"("image_id");
CREATE INDEX "resource_owner_id_index" on "public"."resource"("owner_id");
CREATE INDEX "resource_status_id_index" on "public"."resource"("status_id");

CREATE INDEX "resource_creator_id_index" on "public"."resource"("creator_id");
CREATE INDEX "resource_updater_id_index" on "public"."resource"("updater_id");
CREATE INDEX "resource_tenant_id_index" on "public"."resource"("tenant_id");

CREATE TABLE "public"."role" (
    "id"                serial NOT NULL,
    "role_type_id"      int4 NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    UNIQUE("name", "tenant_id"),
    PRIMARY KEY("id")
);

CREATE INDEX "role_role_type_id_index" on "public"."role"("role_type_id");
CREATE INDEX "role_name_index" on "public"."role"("name");

CREATE INDEX "role_creator_id_index" on "public"."role"("creator_id");
CREATE INDEX "role_updater_id_index" on "public"."role"("updater_id");
CREATE INDEX "role_tenant_id_index" on "public"."role"("tenant_id");

CREATE TABLE "public"."role_operation" (
    "id"                serial NOT NULL,
    "role_id"           int4 NOT NULL,
    "operation_id"      int4 NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "role_operation_role_id_index" on "public"."role_operation"("role_id");
CREATE INDEX "role_operation_operation_id_index" on "public"."role_operation"("operation_id");

CREATE INDEX "role_operation_creator_id_index" on "public"."role_operation"("creator_id");
CREATE INDEX "role_operation_updater_id_index" on "public"."role_operation"("updater_id");
CREATE INDEX "role_operation_tenant_id_index" on "public"."role_operation"("tenant_id");

CREATE TABLE "public"."session"  (
    "id"                serial NOT NULL,
    "jsessionid"        varchar(500) NOT NULL,
    "host" 	        varchar(4000) NULL,
    "user_agent"        varchar(4000) NULL,
    "request_url"       varchar(4000) NULL,
    "referer"           varchar(4000) NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    PRIMARY KEY("id")
);

CREATE TABLE "public"."small_object" (
    "id"                serial NOT NULL,
    "name"              varchar(100) NOT NULL,
    "description"       varchar(4000) NULL,
    "filename"          varchar(250) NULL,
    "content"           bytea NOT NULL,
    "mime_type"         varchar(100) NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "small_object_name_index" on "public"."small_object"("name");

CREATE INDEX "small_object_creator_id_index" on "public"."small_object"("creator_id");
CREATE INDEX "small_object_updater_id_index" on "public"."small_object"("updater_id");
CREATE INDEX "small_object_tenant_id_index" on "public"."small_object"("tenant_id");

CREATE TABLE "public"."storage" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "name"              varchar(256) NOT NULL,
    "description"       varchar(4000) NULL,
    "type"              varchar(256) NOT NULL,
    "location"          varchar(256) NOT NULL,
    "capacity"          int4,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "storage_asset_type_id_index" on "public"."storage"("asset_type_id");
CREATE INDEX "storage_name_index" on "public"."storage"("name");
CREATE INDEX "storage_location_index" on "public"."storage"("location");

CREATE INDEX "storage_creator_id_index" on "public"."storage"("creator_id");
CREATE INDEX "storage_updater_id_index" on "public"."storage"("updater_id");
CREATE INDEX "storage_tenant_id_index" on "public"."storage"("tenant_id");

CREATE TABLE "public"."storage_file" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "storage_id"        int4 NULL,
    "source"            varchar(256) NULL,
    "location"          varchar(256) NOT NULL, /* lfs://storage_id/adfad/a */
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "storage_file_asset_type_id_index" on "public"."storage_file"("asset_type_id");
CREATE INDEX "storage_file_storage_id_index" on "public"."storage_file"("storage_id");

CREATE INDEX "storage_file_creator_id_index" on "public"."storage_file"("creator_id");
CREATE INDEX "storage_file_updater_id_index" on "public"."storage_file"("updater_id");
CREATE INDEX "storage_file_tenant_id_index" on "public"."storage_file"("tenant_id");

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

CREATE TABLE "public"."tenant" (
    "id"                serial NOT NULL,
    "uuid"              uuid NOT NULL,
    "url_identifier"    varchar(250) NOT NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "email"             varchar(250) NULL,
    "image"             bytea NULL,
    "enabled"           bool NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "tenant_uuid_index" on "public"."tenant"("uuid");
CREATE INDEX "tenant_name_index" on "public"."tenant"("name");

CREATE INDEX "tenant_creator_id_index" on "public"."tenant"("creator_id");
CREATE INDEX "tenant_updater_id_index" on "public"."tenant"("updater_id");

CREATE TABLE "public"."tool" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
--    "uuid"              uuid NOT NULL,
--    "version_number"    int4 NOT NULL,
--    "key"               varchar(250) NOT NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "email"             varchar(250) NULL,
--    "content"           bytea NOT NULL,
    -- The fields below to be removed
    "template_id"       int4 NULL,
    "pre_filter"        varchar(4000) NULL,
    "command_line"      varchar(4000) NULL,
    "post_filter"       varchar(4000) NULL,
    -- The fields above to be removed
    "image_id"          int4 NULL,
    "owner_id"          int4 NULL,
    "status_id"         int4 NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
--    UNIQUE("uuid", "version_number"),
--    UNIQUE("key"),
    PRIMARY KEY("id")
);

CREATE INDEX "tool_asset_type_id_index" on "public"."tool"("asset_type_id");
--CREATE INDEX "tool_uuid_index" on "public"."tool"("uuid");
CREATE INDEX "tool_name_index" on "public"."tool"("name");
--CREATE INDEX "tool_key_index" on "public"."tool"("key");
CREATE INDEX "tool_template_id_index" on "public"."tool"("template_id");
CREATE INDEX "tool_image_id_index" on "public"."tool"("image_id");
CREATE INDEX "tool_owner_id_index" on "public"."tool"("owner_id");
CREATE INDEX "tool_status_id_index" on "public"."tool"("status_id");

CREATE INDEX "tool_creator_id_index" on "public"."tool"("creator_id");
CREATE INDEX "tool_updater_id_index" on "public"."tool"("updater_id");
CREATE INDEX "tool_tenant_id_index" on "public"."tool"("tenant_id");

CREATE TABLE "public"."user" (
    "id"                serial NOT NULL,
    "external_source"   varchar(100) NULL,
    "external_id"       varchar(100) NULL,
    "username"          varchar(100) NOT NULL,
    "password"          varchar(100) NOT NULL,
    "salt"              varchar(100) NOT NULL,
    "first_name"        varchar(100) NOT NULL,
    "middle_name"       varchar(100) NULL,
    "last_name"         varchar(100) NOT NULL,
    "email"             varchar(100) NULL,
    "image_id"          int4 NULL,
    "account_non_expired"       bool NOT NULL,
    "account_non_locked"        bool NOT NULL,
    "credentials_non_expired"   bool NOT NULL,
    "enabled"                   bool NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    UNIQUE("username", "tenant_id"),
    UNIQUE("external_source", "external_id", "tenant_id"),
    PRIMARY KEY("id")
);

CREATE INDEX "user_external_source_external_id_index" on "public"."user"("external_source", "external_id");
CREATE INDEX "user_username_index" on "public"."user"("username");
CREATE INDEX "user_first_name_index" on "public"."user"("first_name");
CREATE INDEX "user_last_name_index" on "public"."user"("last_name");
CREATE INDEX "user_email_index" on "public"."user"("email");

CREATE INDEX "user_creator_id_index" on "public"."user"("creator_id");
CREATE INDEX "user_updater_id_index" on "public"."user"("updater_id");
CREATE INDEX "user_tenant_id_index" on "public"."user"("tenant_id");

CREATE TABLE "public"."user_session" (
    "id"                serial NOT NULL,
    "user_id"           int4 NOT NULL,
    "session_id"        int4 NOT NULL,
    "creator_id"        int4 NULL,
    "updater_id"        int4 NULL,
    "time_created"      timestamp NULL,
    "time_updated"      timestamp NULL,
    "tenant_id"         int4 NULL,
    PRIMARY KEY("id")
);

CREATE INDEX "user_session_user_id_index" on "public"."user_session"("user_id");
CREATE INDEX "user_session_session_id_index" on "public"."user_session"("session_id");

CREATE INDEX "user_session_creator_id_index" on "public"."user_session"("creator_id");
CREATE INDEX "user_session_updater_id_index" on "public"."user_session"("updater_id");
CREATE INDEX "user_session_tenant_id_index" on "public"."user_session"("tenant_id");

CREATE TABLE "public"."vocabulary" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "uuid"              uuid NOT NULL,
    "version_number"    uuid NOT NULL,
    "domain"            varchar(250) NOT NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
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

CREATE INDEX "vocabulary_asset_type_id_index" on "public"."vocabulary"("asset_type_id");
CREATE INDEX "vocabulary_uuid_index" on "public"."vocabulary"("uuid");
CREATE INDEX "vocabulary_domain_index" on "public"."vocabulary"("domain");
CREATE INDEX "vocabulary_name_index" on "public"."vocabulary"("name");
CREATE INDEX "vocabulary_image_id_index" on "public"."vocabulary"("image_id");
CREATE INDEX "vocabulary_owner_id_index" on "public"."vocabulary"("owner_id");
CREATE INDEX "vocabulary_status_id_index" on "public"."vocabulary"("status_id");

CREATE INDEX "vocabulary_creator_id_index" on "public"."vocabulary"("creator_id");
CREATE INDEX "vocabulary_updater_id_index" on "public"."vocabulary"("updater_id");
CREATE INDEX "vocabulary_tenant_id_index" on "public"."vocabulary"("tenant_id");

CREATE TABLE "public"."workflow" (
    "id"                serial NOT NULL,
    "asset_type_id"     int4 NULL,
    "resource_id"       int4 NULL,
    "name"              varchar(250) NOT NULL,
    "description"       varchar(4000) NULL,
    "email"             varchar(250) NULL,
    "key"               varchar(250) NOT NULL UNIQUE,
    "content"           varchar NULL,
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

CREATE INDEX "workflow_asset_type_id_index" ON "public"."workflow"("asset_type_id");
CREATE INDEX "workflow_resource_id_index" ON "public"."workflow"("resource_id");
CREATE INDEX "workflow_name_index" ON "public"."workflow"("name");
CREATE INDEX "workflow_key_index" ON "public"."workflow"("key");
CREATE INDEX "workflow_image_id_index" ON "public"."workflow"("image_id");
CREATE INDEX "workflow_owner_id_index" ON "public"."workflow"("owner_id");
CREATE INDEX "workflow_status_id_index" ON "public"."workflow"("status_id");

CREATE INDEX "workflow_creator_id_index" ON "public"."workflow"("creator_id");
CREATE INDEX "workflow_updater_id_index" ON "public"."workflow"("updater_id");
CREATE INDEX "workflow_tenant_id_index" ON "public"."workflow"("tenant_id");

-- Add foreign keys
ALTER TABLE "public"."configuration" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."configuration" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."configuration" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."small_object" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."small_object" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."small_object" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."classification" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."classification" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."classification" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."user" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."user" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."user" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."user" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."user_session" ADD FOREIGN KEY("user_id") REFERENCES "public"."user"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."user_session" ADD FOREIGN KEY("session_id") REFERENCES "public"."session"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."user_session" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."user_session" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."user_session" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."group" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."group" ADD FOREIGN KEY("owner_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."group" ADD FOREIGN KEY("classification_id") REFERENCES "public"."classification"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."group" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."group" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."group" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."group_user" ADD FOREIGN KEY("group_id") REFERENCES "public"."group"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."group_user" ADD FOREIGN KEY("user_id") REFERENCES "public"."user"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."group_user" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."group_user" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."group_user" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."project" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."project" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."project" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."project" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."project" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."project" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."project" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."experiment" ADD FOREIGN KEY("project_id") REFERENCES "public"."project"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."experiment" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."experiment" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."experiment" ADD FOREIGN KEY("image_id") REFERENCES "public"."small_object"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."experiment" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."experiment" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."experiment" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."experiment" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."report" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."report" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."report" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."report" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."report" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."report" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."resource" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."resource" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."resource" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."resource" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."resource" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."resource" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."workflow" ADD FOREIGN KEY("resource_id") REFERENCES "public"."resource"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."workflow" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."workflow" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."workflow" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."workflow" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."workflow" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."workflow" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."template" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."template" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."vocabulary" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."storage" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("storage_id") REFERENCES "public"."storage"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."storage_file" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."computational_node" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."computational_node" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."computational_node" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."tool" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."tool" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_asset_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."tool" ADD FOREIGN KEY("owner_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."tool" ADD FOREIGN KEY("template_id") REFERENCES "public"."template"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."tool" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."tool" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."tool" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."role" ADD FOREIGN KEY("role_type_id") REFERENCES "public"."enum_role_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."role" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."role" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."role" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."role_operation" ADD FOREIGN KEY("role_id") REFERENCES "public"."role"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."role_operation" ADD FOREIGN KEY("operation_id") REFERENCES "public"."enum_operation"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."role_operation" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."role_operation" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."role_operation" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."permission" ADD FOREIGN KEY("group_id") REFERENCES "public"."group"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."permission" ADD FOREIGN KEY("user_id") REFERENCES "public"."user"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."permission" ADD FOREIGN KEY("role_type_id") REFERENCES "public"."enum_role_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."permission" ADD FOREIGN KEY("role_id") REFERENCES "public"."role"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."permission" ADD FOREIGN KEY("operation_id") REFERENCES "public"."enum_operation"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."permission" ADD FOREIGN KEY("asset_type_id") REFERENCES "public"."enum_asset_type"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."permission" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."permission" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."permission" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."job" ADD FOREIGN KEY("parent_id") REFERENCES "public"."job"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("group_id") REFERENCES "public"."group"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("user_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("project_id") REFERENCES "public"."project"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("experiment_id") REFERENCES "public"."experiment"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("resource_id") REFERENCES "public"."resource"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("workflow_id") REFERENCES "public"."workflow"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("status_id") REFERENCES "public"."enum_job_status"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;

ALTER TABLE "public"."job_context" ADD FOREIGN KEY("job_id") REFERENCES "public"."job"("id") ON DELETE CASCADE ON UPDATE NO ACTION;
ALTER TABLE "public"."job_context" ADD FOREIGN KEY("creator_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job_context" ADD FOREIGN KEY("updater_id") REFERENCES "public"."user"("id") ON DELETE SET NULL ON UPDATE NO ACTION;
ALTER TABLE "public"."job_context" ADD FOREIGN KEY("tenant_id") REFERENCES "public"."tenant"("id") ON DELETE SET NULL ON UPDATE NO ACTION;


--//@UNDO
-- SQL to undo the change goes here.
ALTER TABLE "public"."job_context" DROP CONSTRAINT "job_context_tenant_id_fkey";
ALTER TABLE "public"."job_context" DROP CONSTRAINT "job_context_updater_id_fkey";
ALTER TABLE "public"."job_context" DROP CONSTRAINT "job_context_creator_id_fkey";
ALTER TABLE "public"."job_context" DROP CONSTRAINT "job_context_job_id_fkey";

ALTER TABLE "public"."job" DROP CONSTRAINT "job_tenant_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_updater_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_creator_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_status_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_workflow_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_resource_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_experiment_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_project_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_user_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_group_id_fkey";
ALTER TABLE "public"."job" DROP CONSTRAINT "job_parent_id_fkey";

ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_tenant_id_fkey";
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_updater_id_fkey";
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_creator_id_fkey";
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_asset_type_id_fkey";
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_operation_id_fkey";
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_role_id_fkey";
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_role_type_id_fkey";
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_user_id_fkey";
ALTER TABLE "public"."permission" DROP CONSTRAINT "permission_group_id_fkey";

ALTER TABLE "public"."role_operation" DROP CONSTRAINT "role_operation_tenant_id_fkey";
ALTER TABLE "public"."role_operation" DROP CONSTRAINT "role_operation_updater_id_fkey";
ALTER TABLE "public"."role_operation" DROP CONSTRAINT "role_operation_creator_id_fkey";
ALTER TABLE "public"."role_operation" DROP CONSTRAINT "role_operation_operation_id_fkey";
ALTER TABLE "public"."role_operation" DROP CONSTRAINT "role_operation_role_id_fkey";

ALTER TABLE "public"."role" DROP CONSTRAINT "role_tenant_id_fkey";
ALTER TABLE "public"."role" DROP CONSTRAINT "role_updater_id_fkey";
ALTER TABLE "public"."role" DROP CONSTRAINT "role_creator_id_fkey";
ALTER TABLE "public"."role" DROP CONSTRAINT "role_role_type_id_fkey";

ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_tenant_id_fkey";
ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_updater_id_fkey";
ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_creator_id_fkey";
ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_template_id_fkey";
ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_owner_id_fkey";
ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_status_id_fkey";
ALTER TABLE "public"."tool" DROP CONSTRAINT "tool_asset_type_id_fkey";

ALTER TABLE "public"."computational_node" DROP CONSTRAINT "computational_node_tenant_id_fkey";
ALTER TABLE "public"."computational_node" DROP CONSTRAINT "computational_node_updater_id_fkey";
ALTER TABLE "public"."computational_node" DROP CONSTRAINT "computational_node_creator_id_fkey";

ALTER TABLE "public"."storage_file" DROP CONSTRAINT "storage_file_tenant_id_fkey";
ALTER TABLE "public"."storage_file" DROP CONSTRAINT "storage_file_updater_id_fkey";
ALTER TABLE "public"."storage_file" DROP CONSTRAINT "storage_file_creator_id_fkey";
ALTER TABLE "public"."storage_file" DROP CONSTRAINT "storage_file_storage_id_fkey";

ALTER TABLE "public"."storage" DROP CONSTRAINT "storage_tenant_id_fkey";
ALTER TABLE "public"."storage" DROP CONSTRAINT "storage_updater_id_fkey";
ALTER TABLE "public"."storage" DROP CONSTRAINT "storage_creator_id_fkey";

ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_tenant_id_fkey";
ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_updater_id_fkey";
ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_creator_id_fkey";
ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_owner_id_fkey";
ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_status_id_fkey";
ALTER TABLE "public"."vocabulary" DROP CONSTRAINT "vocabulary_asset_type_id_fkey";

ALTER TABLE "public"."template" DROP CONSTRAINT "template_tenant_id_fkey";
ALTER TABLE "public"."template" DROP CONSTRAINT "template_updater_id_fkey";
ALTER TABLE "public"."template" DROP CONSTRAINT "template_creator_id_fkey";
ALTER TABLE "public"."template" DROP CONSTRAINT "template_owner_id_fkey";
ALTER TABLE "public"."template" DROP CONSTRAINT "template_status_id_fkey";
ALTER TABLE "public"."template" DROP CONSTRAINT "template_asset_type_id_fkey";

ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_tenant_id_fkey";
ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_updater_id_fkey";
ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_creator_id_fkey";
ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_owner_id_fkey";
ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_status_id_fkey";
ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_asset_type_id_fkey";
ALTER TABLE "public"."workflow" DROP CONSTRAINT "workflow_resource_id_fkey";

ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_tenant_id_fkey";
ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_updater_id_fkey";
ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_creator_id_fkey";
ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_owner_id_fkey";
ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_status_id_fkey";
ALTER TABLE "public"."resource" DROP CONSTRAINT "resource_asset_type_id_fkey";

ALTER TABLE "public"."report" DROP CONSTRAINT "report_tenant_id_fkey";
ALTER TABLE "public"."report" DROP CONSTRAINT "report_updater_id_fkey";
ALTER TABLE "public"."report" DROP CONSTRAINT "report_creator_id_fkey";
ALTER TABLE "public"."report" DROP CONSTRAINT "report_owner_id_fkey";
ALTER TABLE "public"."report" DROP CONSTRAINT "report_status_id_fkey";
ALTER TABLE "public"."report" DROP CONSTRAINT "report_asset_type_id_fkey";

ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_tenant_id_fkey";
ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_updater_id_fkey";
ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_creator_id_fkey";
ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_owner_id_fkey";
ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_image_id_fkey";
ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_status_id_fkey";
ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_asset_type_id_fkey";
ALTER TABLE "public"."experiment" DROP CONSTRAINT "experiment_project_id_fkey";

ALTER TABLE "public"."project" DROP CONSTRAINT "project_tenant_id_fkey";
ALTER TABLE "public"."project" DROP CONSTRAINT "project_updater_id_fkey";
ALTER TABLE "public"."project" DROP CONSTRAINT "project_creator_id_fkey";
ALTER TABLE "public"."project" DROP CONSTRAINT "project_owner_id_fkey";
ALTER TABLE "public"."project" DROP CONSTRAINT "project_image_id_fkey";
ALTER TABLE "public"."project" DROP CONSTRAINT "project_status_id_fkey";
ALTER TABLE "public"."project" DROP CONSTRAINT "project_asset_type_id_fkey";

ALTER TABLE "public"."group_user" DROP CONSTRAINT "group_user_tenant_id_fkey";
ALTER TABLE "public"."group_user" DROP CONSTRAINT "group_user_updater_id_fkey";
ALTER TABLE "public"."group_user" DROP CONSTRAINT "group_user_creator_id_fkey";
ALTER TABLE "public"."group_user" DROP CONSTRAINT "group_user_user_id_fkey";
ALTER TABLE "public"."group_user" DROP CONSTRAINT "group_user_group_id_fkey";

ALTER TABLE "public"."group" DROP CONSTRAINT "group_tenant_id_fkey";
ALTER TABLE "public"."group" DROP CONSTRAINT "group_updater_id_fkey";
ALTER TABLE "public"."group" DROP CONSTRAINT "group_creator_id_fkey";
ALTER TABLE "public"."group" DROP CONSTRAINT "group_classification_id_fkey";
ALTER TABLE "public"."group" DROP CONSTRAINT "group_owner_id_fkey";
ALTER TABLE "public"."group" DROP CONSTRAINT "group_image_id_fkey";

ALTER TABLE "public"."user_session" DROP CONSTRAINT "user_session_tenant_id_fkey";
ALTER TABLE "public"."user_session" DROP CONSTRAINT "user_session_updater_id_fkey";
ALTER TABLE "public"."user_session" DROP CONSTRAINT "user_session_creator_id_fkey";
ALTER TABLE "public"."user_session" DROP CONSTRAINT "user_session_session_id_fkey";
ALTER TABLE "public"."user_session" DROP CONSTRAINT "user_session_user_id_fkey";

ALTER TABLE "public"."user" DROP CONSTRAINT "user_tenant_id_fkey";
ALTER TABLE "public"."user" DROP CONSTRAINT "user_updater_id_fkey";
ALTER TABLE "public"."user" DROP CONSTRAINT "user_creator_id_fkey";
ALTER TABLE "public"."user" DROP CONSTRAINT "user_image_id_fkey";

ALTER TABLE "public"."classification" DROP CONSTRAINT "classification_tenant_id_fkey";
ALTER TABLE "public"."classification" DROP CONSTRAINT "classification_updater_id_fkey";
ALTER TABLE "public"."classification" DROP CONSTRAINT "classification_creator_id_fkey";

ALTER TABLE "public"."small_object" DROP CONSTRAINT "small_object_tenant_id_fkey";
ALTER TABLE "public"."small_object" DROP CONSTRAINT "small_object_updater_id_fkey";
ALTER TABLE "public"."small_object" DROP CONSTRAINT "small_object_creator_id_fkey";

ALTER TABLE "public"."configuration" DROP CONSTRAINT "configuration_tenant_id_fkey";
ALTER TABLE "public"."configuration" DROP CONSTRAINT "configuration_updater_id_fkey";
ALTER TABLE "public"."configuration" DROP CONSTRAINT "configuration_creator_id_fkey";

DROP TABLE "public"."workflow";
DROP TABLE "public"."vocabulary";
DROP TABLE "public"."user_session";
DROP TABLE "public"."user";
DROP TABLE "public"."tool";
DROP TABLE "public"."tenant";
DROP TABLE "public"."template";
DROP TABLE "public"."storage_file";
DROP TABLE "public"."storage";
DROP TABLE "public"."small_object";
DROP TABLE "public"."session";
DROP TABLE "public"."role_operation";
DROP TABLE "public"."role";
DROP TABLE "public"."resource";
DROP TABLE "public"."report";
DROP TABLE "public"."project";
DROP TABLE "public"."permission";
DROP TABLE "public"."job_context";
DROP TABLE "public"."job";
DROP TABLE "public"."group_user";
DROP TABLE "public"."group";
DROP TABLE "public"."experiment";
DROP TABLE "public"."enum_role_type";
DROP TABLE "public"."enum_operation";
DROP TABLE "public"."enum_job_status";
DROP TABLE "public"."enum_asset_type";
DROP TABLE "public"."enum_asset_status";
DROP TABLE "public"."configuration";
DROP TABLE "public"."computational_node";
DROP TABLE "public"."classification";
