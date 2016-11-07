/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.domain.listener;

import edu.purdue.cybercenter.dm.domain.CrisAsset;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.util.EnumAssetStatus;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

/**
 *
 * @author jiaxu
 */
public class CrisAssetListener {

    @PrePersist
    void onPrePersist(CrisAsset crisAsset) {
        if (crisAsset.getStatusId() == null) {
            crisAsset.setStatusId(EnumAssetStatus.Operational.getIndex());
        }
        setOwner(crisAsset);
    }

    @PostPersist
    void onPostPersist(CrisAsset crisAsset) {
    }

    @PreUpdate
    void onPreUpdate(CrisAsset crisAsset) {
    }

    @PostUpdate
    void onPostUpdate(CrisAsset crisAsset) {
    }

    @PreRemove
    void onPreRemove(CrisAsset crisAsset) {
    }

    @PostRemove
    void onPostRemove(CrisAsset crisAsset) {
    }

    @PostLoad
    void onPostLoad(CrisAsset crisAsset) {
    }

    private void setOwner(CrisAsset crisAsset) {
        Integer groupId = edu.purdue.cybercenter.dm.threadlocal.GroupId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        Group group = Group.findGroup(groupId);
        crisAsset.setIsGroupOwner(group == null ? null : group.getIsGroupOwner());
        if (crisAsset.getIsGroupOwner() != null && crisAsset.getIsGroupOwner()) {
            crisAsset.setOwnerId(groupId);
        } else if (crisAsset.getIsGroupOwner() != null && !crisAsset.getIsGroupOwner()) {
            crisAsset.setOwnerId(userId);
        } else {
            crisAsset.setOwnerId(null);
        }
    }

}
