/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.domain;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author jiaxu
 */
public interface CrisEntity extends Serializable {

    Integer getId();

    void setId(Integer id);

    Integer getTenantId();

    void setTenantId(Integer tenantId);

    Integer getCreatorId();

    void setCreatorId(Integer creatorId);

    Integer getUpdaterId();

    void setUpdaterId(Integer updaterId);

    Date getTimeCreated();

    void setTimeCreated(Date date);

    Date getTimeUpdated();

    void setTimeUpdated(Date date);

    String get$ref();

    void persist();

    <T extends CrisEntity> T merge();

    void remove();

    void flush();

    void refresh();

    void clear();
}
