package edu.purdue.cybercenter.dm.domain;

import flexjson.ObjectFactory;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

@Filters({
    @Filter(name = "tenantFilter", condition = "(id = :tenantId)")
})
@Entity
@Table(schema = "public", name = "tenant")
@Audited
@Configurable
public class Tenant implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, updatable = false)
    private Integer id;

    @Override
    public String toString() {
        return this.getName();
    }

    @PrePersist
    void prePersist() {
        Date date = new Date();

        this.setTimeCreated(date);
        this.setTimeUpdated(date);
    }

    @PreUpdate
    void preUpdate() {
        this.setTimeUpdated(new Date());
    }

    @Column(name = "uuid")
    @Type(type = "pg-uuid")
    private UUID uuid;

    /*********************************************************
     * Json
     * @return
     *********************************************************/
    @JsonProperty
    public String get$ref() {
        return this.getClass().getSimpleName().toLowerCase() + "s" + "/" + id;
    }

    public static Map<Class, ObjectFactory> getUseClasses(String ctxPath) {
        Map<Class, ObjectFactory> useClasses = new HashMap<>();
        return useClasses;
    }

    @Column(name = "url_identifier", length = 250)
    @NotNull
    private String urlIdentifier;

    @Column(name = "name", length = 250)
    @NotNull
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "email", length = 250)
    private String email;

    @Column(name = "image")
    private byte[] image;

    @Column(name = "enabled")
    @NotNull
    private boolean enabled;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "updater_id")
    private Integer updaterId;

    @Column(name = "time_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeCreated;

    @Column(name = "time_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeUpdated;

    public String getUrlIdentifier() {
        return urlIdentifier;
    }

    public void setUrlIdentifier(String urlIdentifier) {
        this.urlIdentifier = urlIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getUpdaterId() {
        return updaterId;
    }

    public void setUpdaterId(Integer updaterId) {
        this.updaterId = updaterId;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    public Date getTimeUpdated() {
        return timeUpdated;
    }

    public void setTimeUpdated(Date timeUpdated) {
        this.timeUpdated = timeUpdated;
    }

    @PersistenceContext
    transient EntityManager entityManager;

    public static final EntityManager entityManager() {
        EntityManager em = new Tenant().entityManager;
        if (em == null) {
            throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        }
        return em;
    }

    public static long countTenants() {
        return entityManager().createQuery("SELECT COUNT(o) FROM Tenant o", Long.class).getSingleResult();
    }

    public static List<Tenant> findAllTenants() {
        return entityManager().createQuery("SELECT o FROM Tenant o", Tenant.class).getResultList();
    }

    public static Tenant findTenant(Integer id) {
        if (id == null) {
            return null;
        }
        return entityManager().find(Tenant.class, id);
    }

    public static List<Tenant> findTenantEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("SELECT o FROM Tenant o", Tenant.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    @Transactional
    public void persist() {
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        this.entityManager.persist(this);
    }

    @Transactional
    public void remove() {
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            Tenant attached = Tenant.findTenant(this.id);
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    public void flush() {
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        this.entityManager.flush();
    }

    @Transactional
    public void refresh() {
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        this.entityManager.refresh(this);
    }

    @Transactional
    public void clear() {
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        this.entityManager.clear();
    }

    @Transactional
    public Tenant merge() {
        if (this.entityManager == null) {
            this.entityManager = entityManager();
        }
        Tenant merged = this.entityManager.merge(this);
        this.entityManager.flush();
        return merged;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
