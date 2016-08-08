@FilterDefs({
    // General
    @FilterDef(name="enabledFilter", parameters={@ParamDef(name="enabled", type="java.lang.Boolean")}, defaultCondition="enabled = :enabled"),
    @FilterDef(name="timeBeforeFilter", parameters={@ParamDef(name="time", type="java.util.Date")}, defaultCondition="time_updated < :time"),
    @FilterDef(name="timeBetweenFilter", parameters={@ParamDef(name="beginTime", type="java.util.Date"), @ParamDef(name="endTime", type="java.util.Date")}, defaultCondition="time_updated >= :beginTime and time_updated < :endTime"),
    @FilterDef(name="timeAfterFilter", parameters={@ParamDef(name="time", type="java.util.Date")}, defaultCondition="time_updated >= :time"),
    @FilterDef(name="assetStatusFilter", parameters={@ParamDef(name="statusIds", type="java.lang.Integer")}, defaultCondition="status_id in (:statusIds)"),
    @FilterDef(name="accountFilter", parameters={@ParamDef(name="accountId", type="java.lang.Integer")}, defaultCondition="account_id = :accountId"),
    @FilterDef(name="userFilter", parameters={@ParamDef(name="userId", type="java.lang.Integer")}, defaultCondition="user_id = :userId"),
    @FilterDef(name="groupFilter", parameters={@ParamDef(name="groupId", type="java.lang.Integer")}, defaultCondition="group_id = :groupId"),
    @FilterDef(name="toolFilter", parameters={@ParamDef(name="toolId", type="java.lang.Integer")}, defaultCondition="tool_id = :toolId"),
    @FilterDef(name="workflowFilter", parameters={@ParamDef(name="workflowId", type="java.lang.Integer")}, defaultCondition="workflow_id = :workflowId"),
    @FilterDef(name="serviceFilter", parameters={@ParamDef(name="serviceId", type="java.lang.Integer")}, defaultCondition="service_id = :serviceId"),
    @FilterDef(name="resourceFilter", parameters={@ParamDef(name="resourceId", type="java.lang.Integer")}, defaultCondition="resource_id = :resourceId"),
    @FilterDef(name="contentFilter", parameters={@ParamDef(name="contentId", type="java.lang.Integer")}, defaultCondition="content_id = :contentId"),
    @FilterDef(name="jobContextFilter", parameters={@ParamDef(name="jobContextId", type="java.lang.Integer")}, defaultCondition="jobContext_id = :jobContextId"),
    @FilterDef(name="projectFilter", parameters={@ParamDef(name="projectId", type="java.lang.Integer")}, defaultCondition="project_id = :projectId"),
    @FilterDef(name="jobFilter", parameters={@ParamDef(name="jobId", type="java.lang.Integer")}, defaultCondition="job_id = :jobId"),
    @FilterDef(name="jobStatusFilter", parameters={@ParamDef(name="statusIds", type="java.lang.Integer")}, defaultCondition="status_id in (:statusIds)"),
    @FilterDef(name="jobTopLevelFilter", defaultCondition="id = parent_id"),
    @FilterDef(name="uiFilter", parameters={@ParamDef(name="uiId", type="java.lang.Integer")}, defaultCondition="ui_id = :uiId"),
    @FilterDef(name="uiStatusFilter", parameters={@ParamDef(name="statusIds", type="java.lang.Integer")}, defaultCondition="status_id in (:statusIds)"),
    @FilterDef(name="tenantFilter", parameters={@ParamDef(name="tenantId", type="java.lang.Integer")}, defaultCondition="tenant_id = :tenantId"),

    // For Classification

    // For ComputationalNode

    // For Configuration

    // For Experiment
    @FilterDef(name = "experimentInProjectFilter", parameters = { @ParamDef(name = "projectId", type = "java.lang.Integer") }, defaultCondition = "(id in (select distinct e.id from experiment e where e.project_id = :projectId))"),
    @FilterDef(name = "experimentNotInProjectFilter", parameters = { @ParamDef(name = "projectId", type = "java.lang.Integer") }, defaultCondition = "(id in (select e1.id from public.experiment e1 where e1.id not in (select distinct e.id from experiment e where e.project_id = :projectId)))"),

    // For Group

    // For GroupUser

    // For Job
    @FilterDef(name = "myJobsFilter", parameters = { @ParamDef(name = "userIds", type = "java.lang.Integer"), @ParamDef(name = "groupIds", type = "java.lang.Integer"), @ParamDef(name = "workflowIds", type = "java.lang.Integer") }, defaultCondition = "(user_id in (:userIds) or group_id in (:groupIds) or workflow_id in (:workflowIds))"),
    @FilterDef(name = "jobNoRatingFilter", defaultCondition = "rating is null"),

    // For JobContext

    // For Permission

    // For Project

    // For Resource

    // Role

    // For RoleOperation

    // For Session

    // For SmallObject

    // For Shortcut
    //@FilterDef(name = "myShortcutsFilter", parameters = { @ParamDef(name = "userId", type = "java.lang.Integer") }, defaultCondition = "(id in (select distinct p.asset_id from permission p where p.asset_type_id = 14 and p.user_id = :userId))"),

    // For Storage

    // For StorageFile
    @FilterDef(name = "storageFileFilter", parameters = {@ParamDef(name = "storageId", type = "java.lang.Integer")}, defaultCondition = "(storage_id = :storageId)"),

    // For Template
    // can be achieved using the one below: "mostRecentUpdatedTermFilter". just for convenience
    @FilterDef(name="mostRecentUpdatedTemplateFilter", defaultCondition = "(time_updated = (select max(t2.time_updated) from term t2 where t2.tenant_id = tenant_id and t2.uuid = uuid) and is_template = true)"),

    // For Term (template is a type of term with is_template set to true)
    @FilterDef(name="mostRecentUpdatedTermFilter", parameters = { @ParamDef(name = "isTemplate", type = "java.lang.Boolean") }, defaultCondition = "(time_updated = (select max(t2.time_updated) from term t2 where t2.tenant_id = tenant_id and t2.uuid = uuid) and is_template = :isTemplate)"),

    // For Tenant
    @FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenantId", type = "java.lang.Integer")}, defaultCondition = "(tenant_id = :tenantId)"),

    // For Tile
    @FilterDef(name = "myTilesFilter", parameters = { @ParamDef(name = "userId", type = "java.lang.Integer") }, defaultCondition = "(creator_id = :userId)"),

    // For Tool
    @FilterDef(name = "myToolsFilter", parameters = { @ParamDef(name = "userId", type = "java.lang.Integer") }, defaultCondition = "(id in (select distinct p.asset_id from permission p where p.asset_type_id = 2 and p.user_id = :userId))"),

    // For User
    @FilterDef(name = "userInGroupFilter", parameters = { @ParamDef(name = "groupId", type = "java.lang.Integer") }, defaultCondition = "(id in (select distinct gu.user_id from group_user gu where gu.group_id = :groupId))"),
    @FilterDef(name = "userNotInGroupFilter", parameters = { @ParamDef(name = "groupId", type = "java.lang.Integer") }, defaultCondition = "(id in (select u.id from public.user u where u.id not in (select distinct gu.user_id from group_user gu where gu.group_id = :groupId)))"),

    // For Vocabulary
    @FilterDef(name="mostRecentUpdatedVocabularyFilter", defaultCondition = "(time_updated = (select max(t2.time_updated) from vocabulary t2 where t2.tenant_id = tenant_id and t2.uuid = uuid))"),

    // For Workflow
    @FilterDef(name = "workflowInResourceFilter", parameters = { @ParamDef(name = "resourceId", type = "java.lang.Integer") }, defaultCondition = "(id in (select distinct o.id from workflow o where o.resource_id = :resourceId))"),
    @FilterDef(name = "workflowNotInResourceFilter", parameters = { @ParamDef(name = "resourceId", type = "java.lang.Integer") }, defaultCondition = "(id in (select o1.id from public.workflow o1 where o1.id not in (select distinct o.id from workflow o where o.resource_id = :resourceId)))")

    // For ViewPjtd
})

@NamedQueries({
    // For Classification

    // For ComputationalNode

    // For Configuration
    @NamedQuery(name = "Configuration.findPropertyByName", query = "select o from Configuration o where o.name = :name"),
    @NamedQuery(name = "Configuration.findGlobalPropertyByName", query = "select o from Configuration o where o.tenantId is null and o.name = :name"),
    @NamedQuery(name = "Configuration.findGlobals", query = "select o from Configuration o where o.tenantId is null"),

    // For Experiment
    @NamedQuery(name = "Experiment.findAll", query = "select o from Experiment o order by o.name"),
    @NamedQuery(name = "Experiment.findByProject", query = "select o from Experiment o where o.projectId = :projectId order by o.name"),

    // For Group

    // For GroupUser

    // For Job
    @NamedQuery(name = "Job.findByExperiment", query = "select o from Job o where o.experimentId = :experimentId order by o.name"),

    // For JobContext
    @NamedQuery(name = "JobContext.findByJobAndName", query = "select jc from JobContext jc where jc.jobId = :jobId and jc.name = :name order by jc.id"),
    @NamedQuery(name = "JobContext.countByJobTaskUuidVersionName", query = "select Count(jc) from JobContext jc where jc.jobId = :jobId and jc.task = :task and jc.termUuid = :uuid and jc.termVersion = :version and jc.name = :name"),
    @NamedQuery(name = "JobContext.countByUuidVersionName", query = "select Count(jc) from JobContext jc where jc.projectId is null and jc.experimentId is null and jc.jobId is null and jc.termUuid = :uuid and jc.termVersion = :version and jc.name = :name"),
    @NamedQuery(name = "JobContext.countByProjectUuidVersionName", query = "select Count(jc) from JobContext jc where jc.experimentId is null and jc.jobId is null and jc.projectId = :projectId and jc.termUuid = :uuid and jc.termVersion = :version and jc.name = :name"),
    @NamedQuery(name = "JobContext.countByProjectExperimentUuidVersionName", query = "select Count(jc) from JobContext jc where jc.jobId is null and jc.projectId = :projectId and jc.experimentId = :experimentId and jc.termUuid = :uuid and jc.termVersion = :version and jc.name = :name"),

    // For Permission

    // For Project
    @NamedQuery(name = "Project.findAll", query = "select o from Project o order by o.name"),

    // For Report
    @NamedQuery(name = "Report.findByUuid", query = "select o from Report o where o.uuid = :uuid"),

    // For Resource

    // Role

    // For RoleOperation

    // For Session

    // For Shortcut
    @NamedQuery(name = "Shortcut.findByUuid", query = "select o from Shortcut o where o.uuid = :uuid"),

    // For SmallObject

    // For Storage

    // For StorageFile

    // For Template
    /*
    @NamedQuery(name = "Template.findAll", query = "select o from Template o order by o.timeUpdated desc"),
    @NamedQuery(name = "Template.findByUuid", query = "select o from Template o where o.uuid = :uuid order by o.timeUpdated desc"),
    @NamedQuery(name = "Template.findByUuidAndVersionNumber", query = "select o from Template o where o.uuid = :uuid and o.versionNumber = :versionNumber"),
    */

    // For Term
    // The only difference between the Template version and Term version is that the template version requires isTemplate to be true
    @NamedQuery(name = "Term.findAll", query = "select o from Term o order by o.timeUpdated desc"),
    @NamedQuery(name = "Term.findLatest", query = "select o from Term o where o.timeUpdated = (select max(t.timeUpdated) from Term t where t.uuid = o.uuid)"),
    @NamedQuery(name = "Term.findByUuid", query = "select o from Term o where o.uuid = :uuid order by o.timeUpdated desc"),
    @NamedQuery(name = "Term.findByUuidAndVersionNumber", query = "select o from Term o where o.uuid = :uuid and o.versionNumber = :versionNumber"),

    @NamedQuery(name = "Term.findTemplateAll", query = "select o from Term o where o.isTemplate = true order by o.timeUpdated desc"),
    @NamedQuery(name = "Term.findTemplateLatest", query = "select o from Term o where o.isTemplate = true and o.timeUpdated = (select max(t.timeUpdated) from Term t where t.uuid = o.uuid)"),
    @NamedQuery(name = "Term.findTemplateByUuid", query = "select o from Term o where o.isTemplate = true and o.uuid = :uuid order by o.timeUpdated desc"),
    @NamedQuery(name = "Term.findTemplateByUuidAndVersionNumber", query = "select o from Term o where o.isTemplate = true and o.uuid = :uuid and o.versionNumber = :versionNumber"),

    // For Tenant
    @NamedQuery(name = "Tenant.findByUrlIdentifier", query = "select o from Tenant o where o.urlIdentifier = :urlIdentifier"),

    // For Tile
    @NamedQuery(name="Tile.findById",query="select o from Tile o where o.id = :id"),

    // For Tool
    @NamedQuery(name = "Tool.findAll", query = "select o from Tool o order by o.timeUpdated desc"),
    @NamedQuery(name = "Tool.findLatest", query = "select o from Tool o where o.timeUpdated = (select max(t.timeUpdated) from Tool t where t.uuid = o.uuid)"),
    @NamedQuery(name = "Tool.findByKey", query = "select o from Tool o where o.key = :key"),
    @NamedQuery(name = "Tool.findByUuid", query = "select o from Tool o where o.uuid = :uuid order by o.timeUpdated desc"),
    @NamedQuery(name = "Tool.findByUuidAndVersionNumber", query = "select o from Tool o where o.uuid = :uuid and o.versionNumber = :versionNumber"),

    // For User

    // For Vocabulary
    @NamedQuery(name = "Vocabulary.findAll", query = "select o from Vocabulary o order by o.timeUpdated desc"),
    @NamedQuery(name = "Vocabulary.findLatest", query = "select o from Vocabulary o where o.timeUpdated = (select max(v.timeUpdated) from Vocabulary v where v.uuid = o.uuid)"),
    @NamedQuery(name = "Vocabulary.findByUuid", query = "select o from Vocabulary o where o.uuid = :uuid order by o.timeUpdated desc"),
    @NamedQuery(name = "Vocabulary.findByUuidAndVersionNumber", query = "select o from Vocabulary o where o.uuid = :uuid and o.versionNumber = :versionNumber"),

    @NamedQuery(name = "Vocabulary.findTermsAll", query = "select o from Term o where o.vocabularyUuid = :vocabularyUuid order by o.id"),
    @NamedQuery(name = "Vocabulary.findTermsLatest", query = "select o from Term o where o.vocabularyUuid = :vocabularyUuid and o.vocabularyId.timeUpdated <= :timeUpdated and o.timeUpdated = (select max(t.timeUpdated) from Term t where t.uuid = o.uuid and t.vocabularyId.timeUpdated <= :timeUpdated) order by o.id"),

    // For Workflow
    @NamedQuery(name = "Workflow.findAll", query = "select o from Workflow o order by o.name"),
    @NamedQuery(name = "Workflow.findByKey", query = "select o from Workflow o where o.key = :key"),

    // For ViewPjtd
    @NamedQuery(name = "ViewPjtd.findByJobId", query = "select distinct o.taskId, o.taskName from ViewPjtd o where jobId = :jobId"),
    @NamedQuery(name = "ViewPjtd.countByJobId", query = "select count(o.taskId) from ViewPjtd o where jobId = :jobId"),
    @NamedQuery(name = "ViewPjtd.findByTaskId", query = "select o from ViewPjtd o where taskId = :taskId"),
    @NamedQuery(name = "ViewPjtd.countByTaskId", query = "select count(o) from ViewPjtd o where taskId = :taskId")
})

@NamedNativeQueries({
    // WARNING:
    // 1. Do not use this type of queries
    // 2. Last resort: Use "named native query" only if it cannot be implemented with "named query"
    @NamedNativeQuery(name = "Configuration.findPropertyByNameNative", query = "select c.* from configuration c where c.name = :name", resultClass=Configuration.class)
})

@GenericGenerator(name = "uuid-generator", strategy = "uuid") package edu.purdue.cybercenter.dm.domain;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.FilterDefs;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.ParamDef;
