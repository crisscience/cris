package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "job_context")
@Audited
@EntityListeners(CrisEntityListener.class)
@Configurable
public class JobContext extends AbstractCrisEntity {
    private static final long serialVersionUID = 1L;

    @PrePersist
    void prePersist() {
        if (this.getStatus() == null) {
            this.setStatus("new");
        }
    }

    @Column(name = "term_uuid")
    @Type(type = "pg-uuid")
    private UUID termUuid;

    @Column(name = "term_version")
    @Type(type = "pg-uuid")
    private UUID termVersion;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project projectId;

    @ManyToOne
    @JoinColumn(name = "experiment_id", referencedColumnName = "id")
    private Experiment experimentId;

    @ManyToOne
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private Job jobId;

    @Column(name = "task", length = 250)
    private String task;

    @Column(name = "name", length = 250)
    private String name;

    @Column(name = "value")
    private String value;

    @Column(name = "status", length = 50)
    @NotNull
    private String status;

    public Project getProjectId() {
        return projectId;
    }

    public void setProjectId(Project projectId) {
        this.projectId = projectId;
    }

    public Experiment getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Experiment experimentId) {
        this.experimentId = experimentId;
    }

    public Job getJobId() {
        return jobId;
    }

    public void setJobId(Job jobId) {
        this.jobId = jobId;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public UUID getTermUuid() {
        return this.termUuid;
    }

    public void setTermUuid(UUID termUuid) {
        this.termUuid = termUuid;
    }

    public UUID getTermVersion() {
        return termVersion;
    }

    public void setTermVersion(UUID termVersion) {
        this.termVersion = termVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Query
     * @return
     */
    public static long countJobContexts() {
        return entityManager().createQuery("SELECT COUNT(o) FROM JobContext o", Long.class).getSingleResult();
    }

    public static List<JobContext> findAllJobContexts() {
        return entityManager().createQuery("SELECT o FROM JobContext o", JobContext.class).getResultList();
    }

    public static JobContext findJobContext(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(JobContext.class, id);
    }

    public static List<JobContext> findJobContextEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM JobContext o", JobContext.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<JobContext> findByJobId(Job jobId) {
        return entityManager().createQuery("select o from JobContext o where o.jobId = :jobId", JobContext.class).setParameter("jobId", jobId).getResultList();
    }

    public static List<JobContext> findByJobIdAndName(Job jobId, String name) {
        return entityManager().createQuery("select o from JobContext o where o.jobId = :jobId and o.name = :name", JobContext.class).setParameter("jobId", jobId).setParameter("name", name).getResultList();
    }

    public static List<JobContext> findAllLike(String regex) {
        return entityManager().createQuery("select o from JobContext o where o.name like :regex", JobContext.class).setParameter("regex", regex).getResultList();
    }

    /**
     * Json
     * @param ctxPath
     * @return
     */
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(Job.class, fjr);
        useClasses.put(Project.class, fjr);
        useClasses.put(Experiment.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    @Override
    public String toString() {
        return this.getName();
    }

}
