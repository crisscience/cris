/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.domain;

/**
 *
 * @author jiaxu
 */
public interface CrisAsset {

    void setAssetTypeId(Integer assetType);

    Integer getAssetTypeId();

    void setStatusId(Integer assetStatus);

    Integer getStatusId();

    void setName(String name);

    String getName();

    void setDescription(String description);

    String getDescription();

    Group getOwnerId();

    void setOwnerId(Group ownerId);

    SmallObject getImageId();

    void setImageId(SmallObject imageId);

//    UUID getUuid();
//    void setUuid(UUID uuid);
//
//    UUID getVersionNumber();
//    void setVersionNumber(UUID versionNumber);
}
