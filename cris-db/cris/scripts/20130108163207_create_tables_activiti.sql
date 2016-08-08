--// create tables activiti
-- Migration SQL that makes the change goes here.

-- Table: act_re_deployment


CREATE TABLE act_re_deployment
(
  id_ character varying(64) NOT NULL,
  name_ character varying(255),
  deploy_time_ timestamp without time zone,
  CONSTRAINT act_re_deployment_pkey PRIMARY KEY (id_ )
);

-- Table: act_ge_bytearray


CREATE TABLE act_ge_bytearray
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  name_ character varying(255),
  deployment_id_ character varying(64),
  bytes_ bytea,
  CONSTRAINT act_ge_bytearray_pkey PRIMARY KEY (id_ ),
  CONSTRAINT act_fk_bytearr_depl FOREIGN KEY (deployment_id_)
      REFERENCES act_re_deployment (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Index: act_idx_bytear_depl


CREATE INDEX act_idx_bytear_depl
  ON act_ge_bytearray
  (deployment_id_ );

-- Table: act_ge_property


CREATE TABLE act_ge_property
(
  name_ character varying(64) NOT NULL,
  value_ character varying(300),
  rev_ integer,
  CONSTRAINT act_ge_property_pkey PRIMARY KEY (name_ )
);

INSERT INTO act_ge_property(name_, value_, rev_) VALUES ('historyLevel', '2', 1);
INSERT INTO act_ge_property(name_, value_, rev_) VALUES ('next.dbid', '1', 1);
INSERT INTO act_ge_property(name_, value_, rev_) VALUES ('schema.history', 'create(5.6)', 1);
INSERT INTO act_ge_property(name_, value_, rev_) VALUES ('schema.version', '5.6', 1);

-- Table: act_hi_actinst


CREATE TABLE act_hi_actinst
(
  id_ character varying(64) NOT NULL,
  proc_def_id_ character varying(64) NOT NULL,
  proc_inst_id_ character varying(64) NOT NULL,
  execution_id_ character varying(64) NOT NULL,
  act_id_ character varying(255) NOT NULL,
  act_name_ character varying(255),
  act_type_ character varying(255) NOT NULL,
  assignee_ character varying(64),
  start_time_ timestamp without time zone NOT NULL,
  end_time_ timestamp without time zone,
  duration_ bigint,
  CONSTRAINT act_hi_actinst_pkey PRIMARY KEY (id_ )
);

-- Index: act_idx_hi_act_inst_end


CREATE INDEX act_idx_hi_act_inst_end
  ON act_hi_actinst
  (end_time_ );

-- Index: act_idx_hi_act_inst_start


CREATE INDEX act_idx_hi_act_inst_start
  ON act_hi_actinst
  (start_time_ );

-- Table: act_hi_attachment


CREATE TABLE act_hi_attachment
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  user_id_ character varying(255),
  name_ character varying(255),
  description_ character varying(4000),
  type_ character varying(255),
  task_id_ character varying(64),
  proc_inst_id_ character varying(64),
  url_ character varying(4000),
  content_id_ character varying(64),
  CONSTRAINT act_hi_attachment_pkey PRIMARY KEY (id_ )
);

-- Table: act_hi_comment


CREATE TABLE act_hi_comment
(
  id_ character varying(64) NOT NULL,
  type_ character varying(255),
  time_ timestamp without time zone NOT NULL,
  user_id_ character varying(255),
  task_id_ character varying(64),
  proc_inst_id_ character varying(64),
  action_ character varying(255),
  message_ character varying(4000),
  full_msg_ bytea,
  CONSTRAINT act_hi_comment_pkey PRIMARY KEY (id_ )
);

-- Table: act_hi_detail


CREATE TABLE act_hi_detail
(
  id_ character varying(64) NOT NULL,
  type_ character varying(255) NOT NULL,
  proc_inst_id_ character varying(64) NOT NULL,
  execution_id_ character varying(64) NOT NULL,
  task_id_ character varying(64),
  act_inst_id_ character varying(64),
  name_ character varying(255) NOT NULL,
  var_type_ character varying(64),
  rev_ integer,
  time_ timestamp without time zone NOT NULL,
  bytearray_id_ character varying(64),
  double_ double precision,
  long_ bigint,
  text_ character varying(4000),
  text2_ character varying(4000),
  CONSTRAINT act_hi_detail_pkey PRIMARY KEY (id_ )
);

-- Index: act_idx_hi_detail_act_inst


CREATE INDEX act_idx_hi_detail_act_inst
  ON act_hi_detail
  (act_inst_id_ );

-- Index: act_idx_hi_detail_name


CREATE INDEX act_idx_hi_detail_name
  ON act_hi_detail
  (name_ );

-- Index: act_idx_hi_detail_proc_inst


CREATE INDEX act_idx_hi_detail_proc_inst
  ON act_hi_detail
  (proc_inst_id_ );

-- Index: act_idx_hi_detail_time


CREATE INDEX act_idx_hi_detail_time
  ON act_hi_detail
  (time_ );

-- Table: act_hi_procinst


CREATE TABLE act_hi_procinst
(
  id_ character varying(64) NOT NULL,
  proc_inst_id_ character varying(64) NOT NULL,
  business_key_ character varying(255),
  proc_def_id_ character varying(64) NOT NULL,
  start_time_ timestamp without time zone NOT NULL,
  end_time_ timestamp without time zone,
  duration_ bigint,
  start_user_id_ character varying(255),
  start_act_id_ character varying(255),
  end_act_id_ character varying(255),
  CONSTRAINT act_hi_procinst_pkey PRIMARY KEY (id_ ),
  CONSTRAINT act_hi_procinst_proc_def_id__business_key__key UNIQUE (proc_def_id_ , business_key_ ),
  CONSTRAINT act_hi_procinst_proc_inst_id__key UNIQUE (proc_inst_id_ )
);

-- Index: act_idx_hi_pro_i_buskey


CREATE INDEX act_idx_hi_pro_i_buskey
  ON act_hi_procinst
  (business_key_ );

-- Index: act_idx_hi_pro_inst_end


CREATE INDEX act_idx_hi_pro_inst_end
  ON act_hi_procinst
  (end_time_ );

-- Table: act_hi_taskinst


CREATE TABLE act_hi_taskinst
(
  id_ character varying(64) NOT NULL,
  proc_def_id_ character varying(64),
  task_def_key_ character varying(255),
  proc_inst_id_ character varying(64),
  execution_id_ character varying(64),
  name_ character varying(255),
  parent_task_id_ character varying(64),
  description_ character varying(4000),
  owner_ character varying(64),
  assignee_ character varying(64),
  start_time_ timestamp without time zone NOT NULL,
  end_time_ timestamp without time zone,
  duration_ bigint,
  delete_reason_ character varying(4000),
  priority_ integer,
  due_date_ timestamp without time zone,
  CONSTRAINT act_hi_taskinst_pkey PRIMARY KEY (id_ )
);

-- Table: act_id_group


CREATE TABLE act_id_group
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  name_ character varying(255),
  type_ character varying(255),
  CONSTRAINT act_id_group_pkey PRIMARY KEY (id_ )
);

-- Table: act_id_info


CREATE TABLE act_id_info
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  user_id_ character varying(64),
  type_ character varying(64),
  key_ character varying(255),
  value_ character varying(255),
  password_ bytea,
  parent_id_ character varying(255),
  CONSTRAINT act_id_info_pkey PRIMARY KEY (id_ )
);

-- Table: act_id_user


CREATE TABLE act_id_user
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  first_ character varying(255),
  last_ character varying(255),
  email_ character varying(255),
  pwd_ character varying(255),
  picture_id_ character varying(64),
  CONSTRAINT act_id_user_pkey PRIMARY KEY (id_ )
);

-- Table: act_id_membership


CREATE TABLE act_id_membership
(
  user_id_ character varying(64) NOT NULL,
  group_id_ character varying(64) NOT NULL,
  CONSTRAINT act_id_membership_pkey PRIMARY KEY (user_id_ , group_id_ ),
  CONSTRAINT act_fk_memb_group FOREIGN KEY (group_id_)
      REFERENCES act_id_group (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT act_fk_memb_user FOREIGN KEY (user_id_)
      REFERENCES act_id_user (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Index: act_idx_memb_group


CREATE INDEX act_idx_memb_group
  ON act_id_membership
  (group_id_ );

-- Index: act_idx_memb_user


CREATE INDEX act_idx_memb_user
  ON act_id_membership
  (user_id_ );

-- Table: act_re_procdef


CREATE TABLE act_re_procdef
(
  id_ character varying(64) NOT NULL,
  category_ character varying(255),
  name_ character varying(255),
  key_ character varying(255),
  version_ integer,
  deployment_id_ character varying(64),
  resource_name_ character varying(4000),
  dgrm_resource_name_ character varying(4000),
  has_start_form_key_ boolean,
  CONSTRAINT act_re_procdef_pkey PRIMARY KEY (id_ )
);

-- Table: act_ru_execution


CREATE TABLE act_ru_execution
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  proc_inst_id_ character varying(64),
  business_key_ character varying(255),
  parent_id_ character varying(64),
  proc_def_id_ character varying(64),
  super_exec_ character varying(64),
  act_id_ character varying(255),
  is_active_ boolean,
  is_concurrent_ boolean,
  is_scope_ boolean,
  CONSTRAINT act_ru_execution_pkey PRIMARY KEY (id_ ),
  CONSTRAINT act_fk_exe_parent FOREIGN KEY (parent_id_)
      REFERENCES act_ru_execution (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT act_fk_exe_procinst FOREIGN KEY (proc_inst_id_)
      REFERENCES act_ru_execution (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT act_fk_exe_super FOREIGN KEY (super_exec_)
      REFERENCES act_ru_execution (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT act_ru_execution_proc_def_id__business_key__key UNIQUE (proc_def_id_ , business_key_ )
);

-- Index: act_idx_exe_parent


CREATE INDEX act_idx_exe_parent
  ON act_ru_execution
  (parent_id_ );

-- Index: act_idx_exe_procinst


CREATE INDEX act_idx_exe_procinst
  ON act_ru_execution
  (proc_inst_id_ );

-- Index: act_idx_exe_super


CREATE INDEX act_idx_exe_super
  ON act_ru_execution
  (super_exec_ );

-- Index: act_idx_exec_buskey


CREATE INDEX act_idx_exec_buskey
  ON act_ru_execution
  (business_key_ );

-- Table: act_ru_task


CREATE TABLE act_ru_task
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  execution_id_ character varying(64),
  proc_inst_id_ character varying(64),
  proc_def_id_ character varying(64),
  name_ character varying(255),
  parent_task_id_ character varying(64),
  description_ character varying(4000),
  task_def_key_ character varying(255),
  owner_ character varying(64),
  assignee_ character varying(64),
  delegation_ character varying(64),
  priority_ integer,
  create_time_ timestamp without time zone,
  due_date_ timestamp without time zone,
  CONSTRAINT act_ru_task_pkey PRIMARY KEY (id_ ),
  CONSTRAINT act_fk_task_exe FOREIGN KEY (execution_id_)
      REFERENCES act_ru_execution (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT act_fk_task_procdef FOREIGN KEY (proc_def_id_)
      REFERENCES act_re_procdef (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT act_fk_task_procinst FOREIGN KEY (proc_inst_id_)
      REFERENCES act_ru_execution (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Index: act_idx_task_create


CREATE INDEX act_idx_task_create
  ON act_ru_task
  (create_time_ );

-- Index: act_idx_task_exec


CREATE INDEX act_idx_task_exec
  ON act_ru_task
  (execution_id_ );

-- Index: act_idx_task_procdef


CREATE INDEX act_idx_task_procdef
  ON act_ru_task
  (proc_def_id_ );

-- Index: act_idx_task_procinst


CREATE INDEX act_idx_task_procinst
  ON act_ru_task
  (proc_inst_id_ );

-- Table: act_ru_identitylink


CREATE TABLE act_ru_identitylink
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  group_id_ character varying(64),
  type_ character varying(255),
  user_id_ character varying(64),
  task_id_ character varying(64),
  CONSTRAINT act_ru_identitylink_pkey PRIMARY KEY (id_ ),
  CONSTRAINT act_fk_tskass_task FOREIGN KEY (task_id_)
      REFERENCES act_ru_task (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Index: act_idx_ident_lnk_group


CREATE INDEX act_idx_ident_lnk_group
  ON act_ru_identitylink
  (group_id_ );

-- Index: act_idx_ident_lnk_user


CREATE INDEX act_idx_ident_lnk_user
  ON act_ru_identitylink
  (user_id_ );

-- Index: act_idx_tskass_task


CREATE INDEX act_idx_tskass_task
  ON act_ru_identitylink
  (task_id_ );

-- Table: act_ru_job


CREATE TABLE act_ru_job
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  type_ character varying(255) NOT NULL,
  lock_exp_time_ timestamp without time zone,
  lock_owner_ character varying(255),
  exclusive_ boolean,
  execution_id_ character varying(64),
  process_instance_id_ character varying(64),
  retries_ integer,
  exception_stack_id_ character varying(64),
  exception_msg_ character varying(4000),
  duedate_ timestamp without time zone,
  repeat_ character varying(255),
  handler_type_ character varying(255),
  handler_cfg_ character varying(4000),
  CONSTRAINT act_ru_job_pkey PRIMARY KEY (id_ ),
  CONSTRAINT act_fk_job_exception FOREIGN KEY (exception_stack_id_)
      REFERENCES act_ge_bytearray (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Index: act_idx_job_exception


CREATE INDEX act_idx_job_exception
  ON act_ru_job
  (exception_stack_id_ );

-- Table: act_ru_variable


CREATE TABLE act_ru_variable
(
  id_ character varying(64) NOT NULL,
  rev_ integer,
  type_ character varying(255) NOT NULL,
  name_ character varying(255) NOT NULL,
  execution_id_ character varying(64),
  proc_inst_id_ character varying(64),
  task_id_ character varying(64),
  bytearray_id_ character varying(64),
  double_ double precision,
  long_ bigint,
  text_ character varying(4000),
  text2_ character varying(4000),
  CONSTRAINT act_ru_variable_pkey PRIMARY KEY (id_ ),
  CONSTRAINT act_fk_var_bytearray FOREIGN KEY (bytearray_id_)
      REFERENCES act_ge_bytearray (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT act_fk_var_exe FOREIGN KEY (execution_id_)
      REFERENCES act_ru_execution (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT act_fk_var_procinst FOREIGN KEY (proc_inst_id_)
      REFERENCES act_ru_execution (id_) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Index: act_idx_var_bytearray


CREATE INDEX act_idx_var_bytearray
  ON act_ru_variable
  (bytearray_id_ );

-- Index: act_idx_var_exe


CREATE INDEX act_idx_var_exe
  ON act_ru_variable
  (execution_id_ );

-- Index: act_idx_var_procinst


CREATE INDEX act_idx_var_procinst
  ON act_ru_variable
  (proc_inst_id_ );


--//@UNDO
-- SQL to undo the change goes here.
DROP INDEX act_idx_var_procinst;
DROP INDEX act_idx_var_exe;
DROP INDEX act_idx_var_bytearray;
DROP INDEX act_idx_job_exception;
DROP INDEX act_idx_tskass_task;
DROP INDEX act_idx_ident_lnk_user;
DROP INDEX act_idx_ident_lnk_group;
DROP INDEX act_idx_task_procinst;
DROP INDEX act_idx_task_procdef;
DROP INDEX act_idx_task_exec;
DROP INDEX act_idx_task_create;
DROP INDEX act_idx_exec_buskey;
DROP INDEX act_idx_exe_super;
DROP INDEX act_idx_exe_procinst;
DROP INDEX act_idx_exe_parent;
DROP INDEX act_idx_memb_user;
DROP INDEX act_idx_memb_group;
DROP INDEX act_idx_hi_pro_inst_end;
DROP INDEX act_idx_hi_pro_i_buskey;
DROP INDEX act_idx_hi_detail_time;
DROP INDEX act_idx_hi_detail_proc_inst;
DROP INDEX act_idx_hi_detail_name;
DROP INDEX act_idx_hi_detail_act_inst;
DROP INDEX act_idx_hi_act_inst_start;
DROP INDEX act_idx_hi_act_inst_end;
DROP INDEX act_idx_bytear_depl;

DROP TABLE act_ru_variable;
DROP TABLE act_ru_job;
DROP TABLE act_ru_identitylink;
DROP TABLE act_ru_task;
DROP TABLE act_ru_execution;
DROP TABLE act_re_procdef;
DROP TABLE act_id_membership;
DROP TABLE act_id_user;
DROP TABLE act_id_info;
DROP TABLE act_id_group;
DROP TABLE act_hi_taskinst;
DROP TABLE act_hi_procinst;
DROP TABLE act_hi_detail;
DROP TABLE act_hi_comment;
DROP TABLE act_hi_attachment;
DROP TABLE act_hi_actinst;
DROP TABLE act_ge_property;
DROP TABLE act_ge_bytearray;
DROP TABLE act_re_deployment;
