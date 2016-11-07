/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.CrisEntity;
import edu.purdue.cybercenter.dm.security.CustomPermission;
import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author xu222
 */
@Service
public class PermissionService {

    @Autowired
    private JdbcMutableAclService aclService;

    @Transactional
    public void setPermission(int iSid, boolean isPrincipal, Class objectClass, int objectId, String permission, boolean clear) {
        CumulativePermission cumulativePermission = new CumulativePermission();
        switch (permission) {
            case "read":
                cumulativePermission.set(CustomPermission.READ);
                break;
            case "update":
                cumulativePermission.set(CustomPermission.WRITE);
                break;
            case "create":
                cumulativePermission.set(CustomPermission.CREATE);
                break;
            case "delete":
                cumulativePermission.set(CustomPermission.DELETE);
                break;
            case "execute":
                cumulativePermission.set(CustomPermission.EXECUTE);
                break;
            case "owner":
                cumulativePermission.set(CustomPermission.OWNER);
                break;
            default:
                cumulativePermission = null;
        }

        if (cumulativePermission != null) {
            setPermission(iSid, isPrincipal, objectClass, objectId, cumulativePermission, clear);
        }
    }

    public void setPermission(int iSid, boolean isPrincipal, Class objectClass, int objectId, CumulativePermission permission, boolean clear) {
        if (objectClass == null) {
            throw new RuntimeException("PermissionService.setPermission(): objectClass cannot be null");
        }

        Sid sid;
        if (isPrincipal) {
            sid = new PrincipalSid("" + iSid);
        } else {
            sid = new GrantedAuthoritySid("" + iSid);
        }

        ObjectIdentity oid = new ObjectIdentityImpl(objectClass.getName(), new Integer(objectId));

        CumulativePermission cumulativePermission = new CumulativePermission();
        cumulativePermission.set(permission);
        try {
            MutableAcl acl = (MutableAcl) aclService.readAclById(oid);
            boolean found = false;
            int aceIdx = 0;
            AccessControlEntry ace = null;
            for (int i = 0; i < acl.getEntries().size(); i++) {
                ace = acl.getEntries().get(i);
                if (ace.getSid().equals(sid)) {
                    found = true;
                    aceIdx = i;
                    break;
                }
            }

            if (found) {
                Permission oldPermission = ace.getPermission();
                cumulativePermission.set(oldPermission);
                acl.updateAce(aceIdx, cumulativePermission);
            } else {
                acl.insertAce(acl.getEntries().size(), cumulativePermission, sid, true);
            }
            aclService.updateAcl(acl);
        } catch (NotFoundException ex) {
            // This is if the object does not exist
            MutableAcl acl = aclService.createAcl(oid);
            acl.insertAce(acl.getEntries().size(), cumulativePermission, sid, true);
            aclService.updateAcl(acl);
        }
    }

    public MutableAcl getAcl(CrisEntity entity) {
        MutableAcl acl;
        if (entity != null) {
            ObjectIdentity oi = new ObjectIdentityImpl(entity.getClass(), entity.getId());
            acl = (MutableAcl) aclService.readAclById(oi);
        } else {
            acl = null;
        }
        return acl;
    }

    public MutableAcl createAcl(CrisEntity entity) {
        MutableAcl acl;
        if (entity != null) {
            ObjectIdentity oi = new ObjectIdentityImpl(entity.getClass(), entity.getId());
            acl = aclService.createAcl(oi);
        } else {
            acl = null;
        }
        return acl;
    }

    public MutableAcl getOrCreateAcl(CrisEntity entity) {
        MutableAcl acl;
        if (entity != null) {
            ObjectIdentity oi = new ObjectIdentityImpl(entity.getClass(), entity.getId());
            try {
                acl = (MutableAcl) aclService.readAclById(oi);
            } catch (NotFoundException nfe) {
                acl = aclService.createAcl(oi);
            }
        } else {
            acl = null;
        }
        return acl;
    }

    public void createAcl(CrisEntity entity, CrisEntity parent) {
        MutableAcl aclParent = getOrCreateAcl(parent);
        MutableAcl aclEntity = getOrCreateAcl(entity);
        if (parent != null) {
            aclEntity.setParent(aclParent);
        }

        aclService.updateAcl(aclEntity);
    }

    public void deleteAcl(CrisEntity entity, boolean deleteChildren) {
        deleteAcl(entity.getClass(), entity.getId(), deleteChildren);
    }

    public void deleteAcl(Class type, Serializable id, boolean deleteChildren) {
        ObjectIdentity oId = new ObjectIdentityImpl(type, id);
        aclService.deleteAcl(oId, deleteChildren);
    }
}
