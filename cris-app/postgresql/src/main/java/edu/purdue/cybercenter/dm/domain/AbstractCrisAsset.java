/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.domain;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author xu222
 */
@MappedSuperclass
@Configurable
public class AbstractCrisAsset extends AbstractCrisEntity implements CrisAsset {
    private static final long serialVersionUID = 1L;

    @Column(name = "asset_type_id", updatable = false)
    private Integer assetTypeId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "name", length = 250)
    @NotNull
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "image_id", referencedColumnName = "id")
    private SmallObject imageId;

    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private Group ownerId;

    @PrePersist
    void prePersistAsset() {
    }

    @PreUpdate
    void preUpdateAsset() {
    }

    @Override
    public void setAssetTypeId(Integer assetTypeId) {
        this.assetTypeId = assetTypeId;
    }

    @Override
    public Integer getAssetTypeId() {
        return this.assetTypeId;
    }

    @Override
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    @Override
    public Integer getStatusId() {
        return this.statusId;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public SmallObject getImageId() {
        return this.imageId;
    }

    @Override
    public void setImageId(SmallObject imageId) {
        this.imageId = imageId;
    }

    @Override
    public Group getOwnerId() {
        return this.ownerId;
    }

    @Override
    public void setOwnerId(Group ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
