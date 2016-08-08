--// create views
-- Migration SQL that makes the change goes here.
CREATE OR REPLACE VIEW "public"."view_pjtd" AS
SELECT DISTINCT 'P' || coalesce(p.id, 0) || 'E' || coalesce(e.id, 0) || 'J' || coalesce(j.id, 0) || 'T' || coalesce(ai.id_, '0') || 'D' || coalesce(jc.id, 0) "id", p.id "project_id", p.name "project_name", e.id "experiment_id", e.name "experiment_name", j.id "job_id", j.name "job_name", Cast(ai.id_ AS int4) "task_id", ai.act_name_ "task_name", ai.start_time_ "task_start_time", jc.id "data_id", jc.term_uuid "data_term_uuid", jc.term_version "data_term_version", jc.name "data_name", jc."value" "data_value", jc.status "data_status", jc.creator_id "data_creator_id", jc.updater_id "data_updater_id", jc.time_created "data_time_created", jc.time_updated "data_time_updated", p.tenant_id "tenant_id"
FROM project p
LEFT JOIN experiment e ON p.id = e.project_id
LEFT JOIN job j ON e.id = j.experiment_id
LEFT JOIN act_hi_procinst pi ON j.id = to_number(pi.business_key_, '9999')
LEFT JOIN act_hi_actinst ai ON pi.id_ = ai.proc_inst_id_
LEFT JOIN job_context jc ON j.id = jc.job_id AND ai.act_id_ = jc.task
WHERE (ai.id_ IS NOT NULL AND ai.act_name_ IS NOT NULL)
    AND (p.status_id = 1)
    AND (e.status_id = 1)
    AND (j.status_id = ANY('{1, 2, 3, 4, 6}'::int[]))
ORDER BY ai.start_time_
;


--//@UNDO
-- SQL to undo the change goes here.
CREATE OR REPLACE VIEW "public"."view_pjtd" AS
SELECT DISTINCT 'P' || coalesce(p.id, 0) || 'E' || coalesce(e.id, 0) || 'J' || coalesce(j.id, 0) || 'T' || coalesce(ai.id_, '0') || 'D' || coalesce(jc.id, 0) "id", p.id "project_id", p.name "project_name", e.id "experiment_id", e.name "experiment_name", j.id "job_id", j.name "job_name", Cast(ai.id_ AS int4) "task_id", ai.act_name_ "task_name", ai.start_time_ "task_start_time", jc.id "data_id", jc.term_uuid "data_term_uuid", jc.term_version "data_term_version", jc.name "data_name", jc."value" "data_value", jc.status "data_status", jc.creator_id "data_creator_id", jc.updater_id "data_updater_id", jc.time_created "data_time_created", jc.time_updated "data_time_updated", p.tenant_id "tenant_id"
FROM project p
LEFT JOIN experiment e ON p.id = e.project_id
LEFT JOIN job j ON e.id = j.experiment_id
LEFT JOIN act_hi_procinst pi ON j.id = to_number(pi.business_key_, '9999')
LEFT JOIN act_hi_actinst ai ON pi.id_ = ai.proc_inst_id_
LEFT JOIN job_context jc ON j.id = jc.job_id AND ai.act_id_ = jc.task
ORDER BY ai.start_time_
;
