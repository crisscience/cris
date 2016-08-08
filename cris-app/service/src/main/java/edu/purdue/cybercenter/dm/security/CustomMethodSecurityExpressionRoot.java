/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Ihsan
 */
@Configurable
public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object target;
    private Object filterObject;
    private Object returnObject;

    @Autowired
    private JdbcMutableAclService aclService;

    public CustomMethodSecurityExpressionRoot(Authentication auth) {
        super(auth);
    }

    public boolean hasGroup(String groupName) {
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

    public boolean hasDatasetPermission(Object item, String permission) {
        System.out.println("**************************************");

        boolean isPermitted;
        if (item == null || !(item instanceof Map)) {
            isPermitted = false;
        } else {
            // 1. figure the oid
            ObjectIdentity oid = getObjectIdentity((Map) item);

            // 2. figure out sid
            UserDetailsAdapter userDetails = (UserDetailsAdapter) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = userDetails.getUser();
            List<Sid> sids = getSids(user);

            // 3. figure permisisons
            Object _id = (Object) ((Map) item).get("_id");
            Permission perm = getPermission(permission, (_id == null));
            List<Permission> perms = new ArrayList<>();
            perms.add(perm);

            System.out.println(permission + " -> " + ((_id == null) ? "create" : "update"));
            System.out.println(oid);
            System.out.println(sids);
            System.out.println(perms);

            // 4 check permission
            try {
                Acl acl = aclService.readAclById(oid, sids);
                isPermitted = acl.isGranted(perms, sids, false);
            } catch (NotFoundException ex) {
                // if nothing found, default to not permitted
                isPermitted = false;
            }

            System.out.println(isPermitted);
        }

        return isPermitted;
    }

    private ObjectIdentity getObjectIdentity(Map value) {
        ObjectIdentity oid;
        if (value != null) {
            Integer experimentId = (Integer) value.get(MetaField.ExperimentId);
            Integer projectId = (Integer) value.get(MetaField.ProjectId);

            if (experimentId != null) {
                oid = new ObjectIdentityImpl(Experiment.class, experimentId);
            } else if (projectId != null) {
                oid = new ObjectIdentityImpl(Project.class, projectId);
            } else {
                // all projects
                oid = new ObjectIdentityImpl(Project.class, 0);
            }
        } else {
            oid = null;
        }
        return oid;
    }

    private List<Sid> getSids(User user) {
        List<Sid> sids = new ArrayList<>();

        if (user != null) {
            // user
            Sid sid = new PrincipalSid(user.getId().toString());
            sids.add(sid);

            // groups
            List<Group> groups = user.getMemberGroups();
            for (Group group : groups) {
                sid = new GrantedAuthoritySid(group.getId().toString());
                sids.add(sid);
            }
        }

        return sids;
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
            default:
        }

        return perm;
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
