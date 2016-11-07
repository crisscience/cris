/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.domain;

/**
 *
 * @author xu222
 */
public class MetaField {
    // maintained by MongoDB
    public static final String Id = "_id";

    // maintained by CRIS
    public static final String TenantUuid = "_tenant_uuid";
    public static final String TenantId = "_tenant_id";
    public static final String TimeCreated = "_time_created";
    public static final String TimeUpdated = "_time_updated";
    public static final String CreatorId = "_creator_id";
    public static final String UpdaterId = "_updater_id";
    public static final String CreatorGroupId = "_creator_group_id";
    public static final String UpdaterGroupId = "_updater_group_id";
    public static final String IsGroupOwner = "_is_group_owner";
    public static final String OwnerId = "_owner_id";
    public static final String ContextId = "_context_id";

    public static final String UserId = "_user_id"; // this represents the current user id and it can be saved as _creator_id or updater_id

    // prefix "current" can be used with _project_id, _experiment_id, _job_id and _task_id
    public static final String Current = "current";

    // depends on workflow/user choices
    public static final String State = "_state";
    public static final String TemplateVersion = "_template_version";
    public static final String ProjectId = "_project_id";
    public static final String ExperimentId = "_experiment_id";
    public static final String JobId = "_job_id";
    public static final String TaskId = "_task_id";
    public static final String User = "_user";
    public static final String Project = "_project";
    public static final String Experiment = "_experiment";
    public static final String Job = "_job";
    public static final String Task = "_task";
    public static final String LocalVariables = "_local_variables";

    public static final String Date = "_date";

    // returned field list: this is used only in query and never saved with data
    public static final String FieldsToReturn = "_fields_to_return";
    public static final String DISTINCT = "_distinct";

    // _dataset_id is always generted by the system if needed
    // In some situations, a user can provide a name and description
    // and in other situations, the system may auto-generated these two fields
    // to provide some context for the dataset.
    public static final String DatasetId  = "_dataset_id";
    public static final String DatasetName = "_dataset_name";
    public static final String DatasetDescription = "_dataset_description";

    // container
    public static final String ContainerTemplateId  = "_container_template_id";
    public static final String ContainerInstanceId  = "_container_instance_id";
    // for each cell of a container
    public static final String CellId  = "_cell_id";
    public static final String CellName  = "_cell_name";
    public static final String CellDescription  = "_cell_description";
    public static final String CellStatus  = "_cell_status";
    public static final String CellStatusComment  = "_cell_status_comment";
}
