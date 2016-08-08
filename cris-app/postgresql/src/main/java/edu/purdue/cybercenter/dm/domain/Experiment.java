package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisAssetListener;
import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.EnumAssetType;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "assetStatusFilter"),
    @Filter(name = "experimentInProjectFilter"),
    @Filter(name = "experimentNotInProjectFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "experiment")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Experiment extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.Experiment.getIndex());
    }

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id", nullable = false)
    private Project projectId;

    @Column(name = "email", length = 250)
    private String email;

    public Project getProjectId() {
        return projectId;
    }

    public void setProjectId(Project projectId) {
        this.projectId = projectId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*********************************************************
     * Query
     * @return
     *********************************************************/
    public static long countExperiments() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Experiment o", Long.class).getSingleResult();
    }

    public static Experiment findExperiment(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Experiment.class, id);
    }

    public static List<Experiment> findExperimentEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Experiment o", Experiment.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    public static List<Experiment> findAllExperiments() {
        return entityManager().createQuery("SELECT o FROM Experiment o ORDER BY o.name", Experiment.class).getResultList();
    }

    public static List<Experiment> findByProjectId(Project projectId) {
        return entityManager().createQuery("select o from Experiment o where projectId = :projectId", Experiment.class).setParameter("projectId", projectId).getResultList();
    }

    public static Long countByProjectId(Project projectId) {
        return entityManager().createQuery("select count(o) from Experiment o where projectId = :projectId", Long.class).setParameter("projectId", projectId).getSingleResult();
    }

    /*********************************************************
     * Json
     * @param ctxPath
     * @return
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(Project.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }
}
