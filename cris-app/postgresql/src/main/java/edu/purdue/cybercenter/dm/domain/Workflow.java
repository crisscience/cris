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
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "workflowInResourceFilter"),
    @Filter(name = "workflowNotInResourceFilter"),
    @Filter(name = "assetStatusFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "workflow")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Workflow extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.Workflow.getIndex());
    }

    public static List<Workflow> findAllWorkflows() {
        return entityManager().createQuery("SELECT o FROM Workflow o ORDER BY o.name", Workflow.class).getResultList();
    }

    public static List<Workflow> findByKey(String key) {
        return entityManager().createQuery("SELECT o FROM Workflow o WHERE o.key = :key ORDER BY o.timeUpdated DESC", Workflow.class).setParameter("key", key).getResultList();
    }

    /*********************************************************
     * Json
     *********************************************************/
    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        JsonRestRef fjr = new JsonRestRef(ctxPath);
        useClasses.put(Resource.class, fjr);
        useClasses.put(User.class, fjr);
        useClasses.put(Tenant.class, fjr);
        return useClasses;
    }

    @ManyToOne
    @JoinColumn(name = "resource_id", referencedColumnName = "id")
    private Resource resourceId;

    @Column(name = "email", length = 250)
    private String email;

    @Column(name = "key", length = 250, unique = true)
    @NotNull
    private String key;

    @Column(name = "version_number")
    @NotNull
    private Integer versionNumber;

    @Column(name = "content")
    private String content;

    public Resource getResourceId() {
        return resourceId;
    }

    public void setResourceId(Resource resourceId) {
        this.resourceId = resourceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static long countWorkflows() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Workflow o", Long.class).getSingleResult();
    }

    public static Workflow findWorkflow(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Workflow.class, id);
    }

    public static List<Workflow> findWorkflowEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Workflow o", Workflow.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
}
