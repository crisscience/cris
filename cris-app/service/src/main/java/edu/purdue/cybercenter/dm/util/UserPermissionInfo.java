/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import java.util.UUID;
import org.springframework.security.acls.model.Permission;

/**
 *
 * @author xu222
 */
public class UserPermissionInfo {
    private Integer userId;
    private Permission permission;
    // null means the scope of the whole workspace
    private Class scopeClass;
    // null means all objects in the scope
    private Integer scopeId;
    // null means all templates
    private UUID templateUuid;

    public UserPermissionInfo() {
        this.userId = null;
        this.permission = null;
        this.scopeClass = null;
        this.scopeId = null;
        this.templateUuid = null;
    }

    public UserPermissionInfo(Integer userId, Permission permission) {
        this.userId = userId;
        this.permission = permission;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * @return the permission
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * @param permission the permission to set
     */
    public void setPermission(Permission permission) {
        this.permission = permission;
    }


    /**
     * @return the scopeClass
     */
    public Class getScopeClass() {
        return scopeClass;
    }

    /**
     * @param scopeClass the scopeClass to set
     */
    public void setScopeClass(Class scopeClass) {
        this.scopeClass = scopeClass;
    }

    /**
     * @return the scopeId
     */
    public Integer getScopeId() {
        return scopeId;
    }

    /**
     * @param scopeId the scopeId to set
     */
    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }

    /**
     * @return the templateUuid
     */
    public UUID getTemplateUuid() {
        return templateUuid;
    }

    /**
     * @param templateUuid the templateUuid to set
     */
    public void setTemplateUuid(UUID templateUuid) {
        this.templateUuid = templateUuid;
    }
}
