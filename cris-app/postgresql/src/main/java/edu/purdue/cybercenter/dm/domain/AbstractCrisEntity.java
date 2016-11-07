/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceContext;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author xu222
 */
@MappedSuperclass
@Configurable
public class AbstractCrisEntity implements CrisEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, updatable = false)
    private Integer id;

    @Column(name = "tenant_id", updatable = false)
    private Integer tenantId;

    @Column(name = "creator_id", updatable = false)
    private Integer creatorId;

    @Column(name = "updater_id")
    private Integer updaterId;

    @Column(name = "time_created", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeCreated;

    @Column(name = "time_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeUpdated;

    @PersistenceContext
    private transient EntityManager entityManager;

    public static EntityManager entityManager() {
        EntityManager em = new AbstractCrisEntity().entityManager;
        if (em == null) {
            throw new IllegalStateException("Entity manager has not been injected (is the Spring Aspects JAR configured as an AJC/AJDT aspects library?)");
        }
        return em;
    }

    @Transactional
    @Override
    public void persist() {
        this.entityManager.persist(this);
    }

    @Transactional
    @Override
    public <T extends CrisEntity> T merge() {
        T merged = (T) this.entityManager.merge(this);
        return merged;
    }

    @Transactional
    @Override
    public void remove() {
        if (this.entityManager.contains(this)) {
            this.entityManager.remove(this);
        } else {
            CrisEntity attached = this.entityManager.find(this.getClass(), id);
            this.entityManager.remove(attached);
        }
    }

    @Transactional
    @Override
    public void flush() {
        this.entityManager.flush();
    }

    @Transactional
    @Override
    public void refresh() {
        this.entityManager.refresh(this);
    }

    @Transactional
    @Override
    public void clear() {
        this.entityManager.clear();
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public Integer getCreatorId() {
        return creatorId;
    }

    @Override
    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    @Override
    public Integer getUpdaterId() {
        return updaterId;
    }

    @Override
    public void setUpdaterId(Integer updaterId) {
        this.updaterId = updaterId;
    }

    @Override
    public Date getTimeCreated() {
        return timeCreated;
    }

    @Override
    public void setTimeCreated(Date timeCreated) {
        this.timeCreated = timeCreated;
    }

    @Override
    public Date getTimeUpdated() {
        return timeUpdated;
    }

    @Override
    public void setTimeUpdated(Date timeUpdated) {
        this.timeUpdated = timeUpdated;
    }

    /*********************************************************
     * Json
     * @return
     *********************************************************/
    @JsonProperty
    @Override
    public String get$ref() {
        return this.getClass().getSimpleName().toLowerCase() + "s" + "/" + this.id;
    }

}
