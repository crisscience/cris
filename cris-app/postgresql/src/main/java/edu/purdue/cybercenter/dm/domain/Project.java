package edu.purdue.cybercenter.dm.domain;

import edu.purdue.cybercenter.dm.domain.listener.CrisAssetListener;
import edu.purdue.cybercenter.dm.domain.listener.CrisEntityListener;
import edu.purdue.cybercenter.dm.util.EnumAssetType;
import edu.purdue.cybercenter.dm.util.JsonRestRef;
import flexjson.ObjectFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;

@Filters({
    @Filter(name = "assetStatusFilter"),
    @Filter(name = "tenantFilter")
})
@Entity
@Table(schema = "public", name = "project")
@Audited
@EntityListeners({CrisEntityListener.class, CrisAssetListener.class})
@Configurable
public class Project extends AbstractCrisAsset {
    private static final long serialVersionUID = 1L;

    @PrePersist
    void prePersist() {
        setAssetTypeId(EnumAssetType.Project.getIndex());
    }

    @OneToMany(mappedBy = "projectId", cascade = CascadeType.REMOVE)
    private Set<Experiment> experiments;

    @Column(name = "email", length = 250)
    private String email;

    public Set<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(Set<Experiment> experiments) {
        this.experiments = experiments;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*********************************************************
     * query
     * @return
     *********************************************************/
    public static long countProjects() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Project o", Long.class).getSingleResult();
    }

    public static Project findProject(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Project.class, id);
    }

    public static List<Project> findAllProjects() {
        return entityManager().createQuery("SELECT o FROM Project o ORDER BY o.name", Project.class).getResultList();
    }

    public static List<Project> findProjectEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Project o", Project.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
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

}
