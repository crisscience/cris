package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisAssetListener;
import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.EnumJobStatus;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "myJobsFilter"),
    @Filter(name = "jobStatusFilter"),
    @Filter(name = "jobTopLevelFilter"),
    @Filter(name = "accountFilter"),
    @Filter(name = "groupFilter"),
    @Filter(name = "userFilter"),
    @Filter(name = "projectFilter"),
    @Filter(name = "resourceFilter"),
    @Filter(name = "serviceFilter"),
    @Filter(name = "jobNoRatingFilter"),
    @Filter(name = "timeBetweenFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "job")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Job extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @OneToMany(mappedBy = "jobId", cascade = CascadeType.REMOVE)
    private Set<JobContext> jobContexts;

    @ManyToOne
    @JoinColumn(name = "experiment_id", referencedColumnName = "id")
    private Experiment experimentId;

    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group groupId;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Job parentId;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project projectId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User userId;

    @ManyToOne
    @JoinColumn(name = "workflow_id", referencedColumnName = "id")
    private Workflow workflowId;

    @Column(name = "parameters")
    private String parameters;

    @Column(name = "end_uri")
    private String endUri;

    @Column(name = "template_uuids")
    private String templateUuids;

    public Job getParent() {
        return getParentId();
    }

    public void setParent(Job parent) {
        setParentId(parent);
    }

    public Set<JobContext> getJobContexts() {
        return jobContexts;
    }

    public void setJobContexts(Set<JobContext> jobContexts) {
        this.jobContexts = jobContexts;
    }

    public Experiment getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(Experiment experimentId) {
        this.experimentId = experimentId;
    }

    public Group getGroupId() {
        return groupId;
    }

    public void setGroupId(Group groupId) {
        this.groupId = groupId;
    }

    public Job getParentId() {
        return parentId;
    }

    public void setParentId(Job parentId) {
        this.parentId = parentId;
    }

    public Project getProjectId() {
        return projectId;
    }

    public void setProjectId(Project projectId) {
        this.projectId = projectId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Workflow getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Workflow workflowId) {
        this.workflowId = workflowId;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getEndUri() {
        return endUri;
    }

    public void setEndUri(String endUri) {
        this.endUri = endUri;
    }

    public Set<String> getTemplateUuids() {
        Set<String> ids = new HashSet<>();
        if (templateUuids != null && !templateUuids.isEmpty()) {
            String[] uuids = templateUuids.split(",");
            for (String uuid : uuids) {
                ids.add(uuid);
            }
        }

        return ids;
    }

    public void setTemplateUuids(Set<String> templateUuids) {
        StringBuilder sb = new StringBuilder();
        if (templateUuids != null) {
            for (String uuid : templateUuids) {
                sb.append(uuid);
                if (sb.length() != 0) {
                    sb.append(",");
                }
            }
        }

        this.templateUuids = sb.toString();
    }

    /*********************************************************
     * Query
     * @return
     *********************************************************/
    public static long countTopLevelJobs() {
        return entityManager().createQuery("select count(o) from Job o where id = parentId", Long.class).getSingleResult();
    }

    public static long countIncompleteTopLevelJobs() {
        return entityManager().createQuery("select count(o) from Job o where id = parentId and (o.statusId.id = 1 or o.statusId.id = 2)", Long.class).getSingleResult();
    }

    public static List<Job> findTopLevelJobs(String orderBy) {
        if (orderBy != null && orderBy.length() != 0) {
            return entityManager().createQuery("select o from Job o where id = parentId", Job.class).getResultList();
        } else {
            return entityManager().createQuery("select o from Job o where id = parentId order by :orderBy", Job.class).setParameter("orderBy", orderBy).getResultList();
        }
    }

    public static List<Job> findByProjectId(Project projectId) {
        return entityManager().createQuery("select o from Job o where projectId = :projectId", Job.class).setParameter("projectId", projectId).getResultList();
    }

    public static Long countByProjectId(Project projectId) {
        return entityManager().createQuery("select count(o) from Job o where projectId = :projectId", Long.class).setParameter("projectId", projectId).getSingleResult();
    }

    public static List<Job> findByExperimentId(Experiment experimentId) {
        return entityManager().createQuery("select o from Job o where experimentId = :experimentId", Job.class).setParameter("experimentId", experimentId).getResultList();
    }

    public static Long countByExperimentId(Experiment experimentId) {
        return entityManager().createQuery("select count(o) from Job o where experimentId = :experimentId", Long.class).setParameter("experimentId", experimentId).getSingleResult();
    }

    public static List<Job> findByName(String name) {
        return entityManager().createQuery("select o from Job o where upper(name) = :name", Job.class).setParameter("name", name.toUpperCase()).getResultList();
    }

    public static Long countByName(String name) {
        return entityManager().createQuery("select count(o) from Job o where upper(name) = :name and statusId <> :statusId", Long.class).setParameter("name", name.toUpperCase()).setParameter("statusId", EnumJobStatus.CANCELED.getIndex()).getSingleResult();
    }

    public static long countJobs() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Job o", Long.class).getSingleResult();
    }

    public static List<Job> findAllJobs() {
        return entityManager().createQuery("SELECT o FROM Job o", Job.class).getResultList();
    }

    public static Job findJob(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Job.class, id);
    }

    public static List<Job> findJobEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Job o", Job.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
