package edu.purdue.cybercenter.dm.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.Temporal;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "view_pjtd")
@Configurable
public class ViewPjtd implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", unique = true, columnDefinition = "varchar", length = 256, insertable = false, updatable = false)
    private String id;

    @Column(name = "project_id", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer projectId;

    @Column(name = "project_name", columnDefinition = "varchar", length = 256, insertable = false, updatable = false)
    private String projectName;

    @Column(name = "experiment_id", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer experimentId;

    @Column(name = "experiment_name", columnDefinition = "varchar", length = 256, insertable = false, updatable = false)
    private String experimentName;

    @Column(name = "job_id", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer jobId;

    @Column(name = "job_name", columnDefinition = "varchar", length = 256, insertable = false, updatable = false)
    private String jobName;

    @Column(name = "task_id", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer taskId;

    @Column(name = "task_name", columnDefinition = "varchar", length = 256, insertable = false, updatable = false)
    private String taskName;

    @Column(name = "task_start_time", insertable = false, updatable = false)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date taskStartTime;

    @Column(name = "data_id", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer dataId;

    @Type(type = "pg-uuid")
    @Column(name = "data_term_uuid", insertable = false, updatable = false)
    private UUID dataTermUuid;

    @Type(type = "pg-uuid")
    @Column(name = "data_term_version", insertable = false, updatable = false)
    private UUID dataTermVersion;

    @Column(name = "data_name", columnDefinition = "varchar", length = 256, insertable = false, updatable = false)
    private String dataName;

    @Column(name = "data_value", columnDefinition = "varchar", length = 4000, insertable = false, updatable = false)
    private String dataValue;

    @Column(name = "data_status", columnDefinition = "varchar", length = 256, insertable = false, updatable = false)
    private String dataStatus;

    @Column(name = "data_creator_id", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer dataCreatorId;

    @Column(name = "data_updater_id", columnDefinition = "int4", insertable = false, updatable = false)
    private Integer dataUpdaterId;

    @Column(name = "data_time_created", insertable = false, updatable = false)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dataTimeCreated;

    @Column(name = "data_time_updated", insertable = false, updatable = false)
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dataTimeUpdated;

    @Column(name = "tenant_id", insertable = false, updatable = false)
    private Integer tenantId;

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static ViewPjtd findViewPjtd(String id) {
        if (id == null) return null;
        return entityManager().find(ViewPjtd.class, id);
    }

    public static List<Object[]> findByJobId(Integer jobId) {
        return entityManager().createQuery("select distinct o.taskId, o.taskName, o.taskStartTime from ViewPjtd o where o.jobId = :jobId order by o.taskStartTime", Object[].class).setParameter("jobId", jobId).getResultList();
    }

    public static Long countByJobId(Integer jobId) {
        return entityManager().createQuery("select count(o.taskId) from ViewPjtd o where o.jobId = :jobId", Long.class).setParameter("jobId", jobId).getSingleResult();
    }

    public static List<ViewPjtd> findByTaskId(Integer taskId) {
        return entityManager().createQuery("select o from ViewPjtd o where o.taskId = :taskId", ViewPjtd.class).setParameter("taskId", taskId).getResultList();
    }

    public static Long countByTaskId(Integer taskId) {
        return entityManager().createQuery("select count(o) from ViewPjtd o where o.taskId = :taskId", Long.class).setParameter("taskId", taskId).getSingleResult();
    }

    @PersistenceContext
    transient EntityManager entityManager;

    public static final EntityManager entityManager() {
        EntityManager em = new ViewPjtd().entityManager;
        if (em == null) {
            throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        }
        return em;
    }

    public static long countViewPjtds() {
        return entityManager().createQuery("SELECT COUNT(o) FROM ViewPjtd o", Long.class).getSingleResult();
    }

    public static List<ViewPjtd> findAllViewPjtds() {
        return entityManager().createQuery("SELECT o FROM ViewPjtd o", ViewPjtd.class).getResultList();
    }

    public static List<ViewPjtd> findViewPjtdEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM ViewPjtd o", ViewPjtd.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void refresh() {
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        this.entityManager.refresh(this);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getExperimentId() {
        return this.experimentId;
    }

    public void setExperimentId(Integer experimentId) {
        this.experimentId = experimentId;
    }

    public String getExperimentName() {
        return this.experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public Integer getJobId() {
        return this.jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return this.jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getTaskId() {
        return this.taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getTaskStartTime() {
        return this.taskStartTime;
    }

    public void setTaskStartTime(Date taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public Integer getDataId() {
        return this.dataId;
    }

    public void setDataId(Integer dataId) {
        this.dataId = dataId;
    }

    public UUID getDataTermUuid() {
        return this.dataTermUuid;
    }

    public void setDataTermUuid(UUID dataTermUuid) {
        this.dataTermUuid = dataTermUuid;
    }

    public UUID getDataTermVersion() {
        return this.dataTermVersion;
    }

    public void setDataTermVersion(UUID dataTermVersion) {
        this.dataTermVersion = dataTermVersion;
    }

    public String getDataName() {
        return this.dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getDataValue() {
        return this.dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

    public String getDataStatus() {
        return this.dataStatus;
    }

    public void setDataStatus(String dataStatus) {
        this.dataStatus = dataStatus;
    }

    public Integer getDataCreatorId() {
        return this.dataCreatorId;
    }

    public void setDataCreatorId(Integer dataCreatorId) {
        this.dataCreatorId = dataCreatorId;
    }

    public Integer getDataUpdaterId() {
        return this.dataUpdaterId;
    }

    public void setDataUpdaterId(Integer dataUpdaterId) {
        this.dataUpdaterId = dataUpdaterId;
    }

    public Date getDataTimeCreated() {
        return this.dataTimeCreated;
    }

    public void setDataTimeCreated(Date dataTimeCreated) {
        this.dataTimeCreated = dataTimeCreated;
    }

    public Date getDataTimeUpdated() {
        return this.dataTimeUpdated;
    }

    public void setDataTimeUpdated(Date dataTimeUpdated) {
        this.dataTimeUpdated = dataTimeUpdated;
    }

    public Integer getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }
}
