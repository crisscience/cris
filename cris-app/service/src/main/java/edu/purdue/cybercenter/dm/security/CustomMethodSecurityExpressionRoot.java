/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import edu.purdue.cybercenter.dm.domain.CrisEntity;
import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.GroupUser;
import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.Shortcut;
import edu.purdue.cybercenter.dm.domain.StorageFile;
import edu.purdue.cybercenter.dm.domain.Term;
import edu.purdue.cybercenter.dm.domain.Tile;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.threadlocal.GroupId;
import edu.purdue.cybercenter.dm.threadlocal.UserId;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Ihsan
 */
public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object target;
    private Object filterObject;
    private Object returnObject;

    private PermissionEvaluator permissionEvaluator;
    private final PermissionFactory permissionFactory = new CustomPermissionFactory();

    public CustomMethodSecurityExpressionRoot(Authentication auth) {
        super(auth);
    }

    public boolean isAdmin() {
        return hasGroup("Admin Group");
    }

    public boolean hasDomainObjectPermission(Object domainObject, Object permission) {
        if (domainObject == null) {
            return false;
        }

        if (isExceptionClass(domainObject.getClass().getName())) {
            return true;
        }

        Permission checkedPermission = toPermission(permission);

        CrisEntity crisEntity;
        if (domainObject.getClass() == Experiment.class) {
            // an Experiment uses the same permission as its associated project
            crisEntity = ((Experiment) domainObject).getProjectId();
        } else if (domainObject.getClass() == GroupUser.class) {
            // a GroupUser uses the same permission as its associated group
            crisEntity = ((GroupUser) domainObject).getGroupId();
        } else if (domainObject.getClass() == StorageFile.class || domainObject.getClass() == Job.class) {
            // a StorageFile/Job are treated like a dataset associated the project
            Integer id;
            Boolean isGroupOwner;
            Integer ownerId;
            Integer projectId;
            if (domainObject.getClass() == StorageFile.class) {
                StorageFile storageFile = (StorageFile) domainObject;
                id = storageFile.getId();
                isGroupOwner = storageFile.getIsGroupOwner();
                ownerId = storageFile.getOwnerId();
                projectId = storageFile.getProjectId();
            } else {
                Job job = (Job) domainObject;
                id = job.getId();
                isGroupOwner = job.getIsGroupOwner();
                ownerId = job.getOwnerId();
                projectId = job.getProjectId().getId();
            }
            Map<String, Object> item = new HashMap<>();
            item.put(MetaField.Id, id);
            item.put(MetaField.IsGroupOwner, isGroupOwner);
            item.put(MetaField.OwnerId, ownerId);
            item.put(MetaField.ProjectId, projectId);
            boolean isPermitted = false;
            if ((checkedPermission.getMask() & CustomPermission.EXECUTE.getMask()) == 0 && projectId != null) {
                // the execute flag should not be set and the projectId must not be null
                isPermitted = isDatasetOwner(item) || hasDatasetPermission(item, permission.toString());
            }
            return isPermitted;
        } else {
            // anything else
            crisEntity = (CrisEntity) domainObject;
        }

        String objectClass = crisEntity.getClass().getName();

        // figure out obejct id
        Integer objectId = crisEntity.getId();
        if (!checkedPermission.equals(CustomPermission.EXECUTE) && !checkedPermission.equals(CustomPermission.OWNER)) {
            // this is for management permissions
            objectId = 0;
        }

        return hasDomainObjectPermission(objectId, objectClass, permission);
    }

    public boolean hasDomainObjectPermission(Serializable targetId, String targetType, Object permission) {
        if (isExceptionClass(targetType)) {
            return true;
        }

        return permissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
    }

    public boolean isDatasetOwner(Object item) {
        boolean isOwner = false;

        if (item != null && (item instanceof Map)) {
            Map<String, Object> map = (Map) item;
            boolean isNew = map.get(MetaField.Id) == null;
            if (!isNew) {
                // must be an existing document
                Boolean isGroupOwner = (Boolean) map.get(MetaField.IsGroupOwner);
                Integer ownerId = (Integer) map.get(MetaField.OwnerId);
                if (isGroupOwner != null) {
                    Integer userId = UserId.get();
                    if (isGroupOwner) {
                        // group owner
                        Group group = Group.findGroup(ownerId);
                        User user = User.findUser(userId);
                        if (group != null && user != null) {
                            isOwner = group.isMember(user);
                        }
                    } else {
                        // user owner
                        isOwner = (userId.equals(ownerId));
                    }
                }
            } else {
                // owner for new document
                Integer groupId = GroupId.get();
                if (groupId != null) {
                    Group group = Group.findGroup(groupId);
                    isOwner = group.getIsGroupOwner() != null;
                }
            }

            if (isOwner) {
                // find out if the project/experiment allows owner operations
                boolean hasOwnerPermission = hasDatasetPermission(item, "owner");
                if (!hasOwnerPermission) {
                    isOwner = false;
                }
            }
        }

        return isOwner;
    }

    public boolean hasDatasetPermission(Object item, String permission) {
        boolean isPermitted;
        if (item == null || !(item instanceof Map)) {
            isPermitted = false;
        } else {
            // 1. figure the oid
            Integer id = (Integer) ((Map) item).get(MetaField.ProjectId);
            String type = Project.class.getName();

            // 2. figure permisisons
            Object _id = (Object) ((Map) item).get(MetaField.Id);
            Permission perm = getPermission(permission, (_id == null));

            // 3 check permission
            isPermitted = permissionEvaluator.hasPermission(authentication, id, type, perm);
        }

        return isPermitted;
    }

    private boolean hasGroup(String groupName) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((UserDetailsAdapter) principal).getUser();

        List<Group> groups = Group.findGroupsByUserId(user.getId());
        for (Group group : groups) {
            if (group.getName().equals(groupName)) {
                return true;
            }
        }
        return false;
    }

    private Permission getPermission(String permission, boolean isCreate) {
        CumulativePermission perm = new CumulativePermission();
        switch (permission) {
            case "read":
                perm.set(BasePermission.READ);
                break;
            case "create":
            case "update":
                if (isCreate) {
                    perm.set(BasePermission.CREATE);
                } else {
                    perm.set(BasePermission.WRITE);
                }
                break;
            case "delete":
                perm.set(BasePermission.DELETE);
                break;
            case "execute":
                perm.set(CustomPermission.EXECUTE);
                break;
            case "owner":
                perm.set(CustomPermission.OWNER);
                break;
            default:
                throw new RuntimeException("Unknown permission: " + permission);
        }

        return perm;
    }

    private Permission toPermission(Object permission) {
        Permission p;

        if (permission instanceof Permission) {
            p = (Permission) permission;
        } else {
            String permString = (String) permission;
            try {
                p = permissionFactory.buildFromName(permString);
            } catch (IllegalArgumentException notfound) {
                p = permissionFactory.buildFromName(permString.toUpperCase());
            }
        }

        if (p != null) {
            return p;
        }

        throw new IllegalArgumentException("Unsupported permission: " + permission);
    }

    private boolean isExceptionClass(String objectClass) {
        return (objectClass.equals(Tile.class.getName())) ||
               (objectClass.equals(Shortcut.class.getName())) ||
               (objectClass.equals(Term.class.getName()));
    }

    @Override
    public void setPermissionEvaluator(PermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    void setThis(Object target) {
        this.target = target;
    }

    @Override
    public Object getThis() {
        return target;
    }
}
