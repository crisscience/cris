/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.domain.listener;

import edu.purdue.cybercenter.dm.domain.CrisEntity;
import java.util.Date;
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
public class CrisEntityListener {

    @PrePersist
    void onPrePersist(CrisEntity crisEntity) {
        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();
        Date date = new Date();

        crisEntity.setTenantId(tenantId);
        crisEntity.setUpdaterId(userId);
        crisEntity.setCreatorId(userId);
        crisEntity.setTimeCreated(date);
        crisEntity.setTimeUpdated(date);
    }

    @PostPersist
    void onPostPersist(CrisEntity crisEntity) {
    }

    @PreUpdate
    void onPreUpdate(CrisEntity crisEntity) {
        Integer userId = edu.purdue.cybercenter.dm.threadlocal.UserId.get();

        crisEntity.setUpdaterId(userId);
        crisEntity.setTimeUpdated(new Date());
    }

    @PostUpdate
    void onPostUpdate(CrisEntity crisEntity) {
    }

    @PreRemove
    void onPreRemove(CrisEntity crisEntity) {
    }

    @PostRemove
    void onPostRemove(CrisEntity crisEntity) {
    }

    @PostLoad
    void onPostLoad(CrisEntity crisEntity) {
    }

}
