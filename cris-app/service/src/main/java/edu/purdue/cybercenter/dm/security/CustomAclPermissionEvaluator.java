/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.GroupUser;
import edu.purdue.cybercenter.dm.domain.Job;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.Shortcut;
import edu.purdue.cybercenter.dm.domain.Term;
import edu.purdue.cybercenter.dm.domain.Tile;
import edu.purdue.cybercenter.dm.domain.User;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Ihsan
 */
public class CustomAclPermissionEvaluator extends AclPermissionEvaluator {

    @Autowired
    private JdbcMutableAclService aclService;
    private final ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = new ObjectIdentityRetrievalStrategyImpl();
    private final SidRetrievalStrategy sidRetrievalStrategy = new CustomSidRetrievalStrategyImpl();
    private final PermissionFactory permissionFactory = new DefaultPermissionFactory();

    public CustomAclPermissionEvaluator(AclService aclService) {
        super(aclService);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object domainObject, Object permission) {
        if (domainObject == null) {
            return false;
        }

        Object object;
        if (domainObject.getClass() == Job.class) {
            // a job uses the same permission as its associated experiment/project
            object = ((Job) domainObject).getExperimentId();
            if (object == null) {
                object = ((Job) domainObject).getProjectId();
            }
            if (object == null) {
                return false;
            }
        } else {
            object = domainObject;
        }

        String objectClass = object.getClass().getName();
        if (isExceptionClass(objectClass)) {
            return true;
        } else if (permission.toString().equals("create")) { // special check for create permission
            return checkPermissionCreate(authentication, object, permission);
        } else {
            if (isClassUserGroupManagement(objectClass) || objectClass.equals(Term.class.getName())) { // For groups and users, permissions are applied at class level
                return checkPermission(authentication, new ObjectIdentityImpl(objectClass.equals(GroupUser.class.getName()) ? Group.class.getName() : objectClass, new Long(0)), permission);
            } else { // For remaining classes, permissions are applied at object level
                return checkPermission(authentication, objectIdentityRetrievalStrategy.getObjectIdentity(object), permission);
            }
        }
    }

    private boolean checkPermissionCreate(Authentication authentication, Object domainObject, Object permission) {

        String objectClass = domainObject.getClass().getName();
        ObjectIdentity oid;
        boolean isProject = false, isExperiment = false, isJob = false;

        if (isClassProjectExperimentJob(objectClass)) {
            if (objectClass.equals(Project.class.getName())) {
                oid = new ObjectIdentityImpl(objectClass, new Long(0));
                isProject = true;
            } else {
                if (objectClass.equals(Job.class.getName())) {
                    Experiment exp = ((Job) domainObject).getExperimentId();
                    oid = new ObjectIdentityImpl(Experiment.class, new Long(exp.getId()));
                    isJob = true;
                } else {
                    Project pro = ((Experiment) domainObject).getProjectId();
                    oid = new ObjectIdentityImpl(Project.class, new Long(pro.getId()));
                    isExperiment = true;
                }
            }
            List<Sid> sids = sidRetrievalStrategy.getSids(authentication);
            List<Permission> requiredPermission = resolvePermission(permission);
            boolean check = false;
            boolean checkJob = false;
            try {
                // Lookup only ACLs for SIDs we're interested in
                Acl acl = aclService.readAclById(oid, sids);
                check = true; // this implies object exists
                boolean match = acl.isGranted(requiredPermission, sids, false);
                return match;
            } catch (NotFoundException nfe) {
                try {
                    if (isProject) {
                        return false;
                    } else if (isExperiment && !check) { //i.e check the parent if object does not exist
                        ObjectIdentity oi = new ObjectIdentityImpl(Project.class, new Long(0));
                        Acl aclAll = aclService.readAclById(oi, sids);
                        if (aclAll.isGranted(requiredPermission, sids, false)) {
                            return true;
                        }
                    } else if (isJob && !check) {
                        Project pro = ((Job) domainObject).getProjectId();
                        ObjectIdentity oi = new ObjectIdentityImpl(Project.class, new Long(pro.getId()));
                        Acl aclAll = aclService.readAclById(oi, sids);
                        checkJob = true;
                        boolean match = aclAll.isGranted(requiredPermission, sids, false);
                        if (match) {
                            return true;
                        }
                    }
                    return false;
                } catch (NotFoundException nfe1) {
                    if (isExperiment) {
                        return false;
                    } else if (isJob && !checkJob) {
                        try {
                            ObjectIdentity oiP = new ObjectIdentityImpl(Project.class, new Long(0));
                            Acl aclP = aclService.readAclById(oiP, sids);
                            if (aclP.isGranted(requiredPermission, sids, false)) {
                                return true;
                            }
                        } catch (NotFoundException nfe2) {
                            return false;
                        }
                    }
                    return false;
                }
            }
        } else {
            return checkPermission(authentication, new ObjectIdentityImpl(objectClass.equals(GroupUser.class.getName()) ? Group.class.getName() : objectClass, new Long(0)), permission);
        }
    }

    private boolean checkPermission(Authentication authentication, ObjectIdentity oid, Object permission) {
        // Obtain the SIDs applicable to the principal
        List<Sid> sids = sidRetrievalStrategy.getSids(authentication);
        List<Permission> requiredPermission = resolvePermission(permission);
        String objectClass = oid.getType();

        ObjectIdentity oidAll = new ObjectIdentityImpl(objectClass, new Long(0));
        boolean check = false;
        try {
            // Lookup only ACLs for SIDs we're interested in
            Acl acl = aclService.readAclById(oid, sids);
            boolean match = acl.isGranted(requiredPermission, sids, false);
            check = true;
            if (match) {
                return true;
            } else {
                Acl aclAll = aclService.readAclById(oidAll, sids);
                if (aclAll.isGranted(requiredPermission, sids, false)) {
                    return true;
                }
            }
        } catch (NotFoundException nfe) {
            if (!check) {
                try {
                    Acl aclAll = aclService.readAclById(oidAll, sids);
                    if (aclAll.isGranted(requiredPermission, sids, false)) {
                        return true;
                    }
                } catch (NotFoundException nfe1) {
                    return false;
                }
            }
        }
        return false;
    }

    private List<Permission> resolvePermission(Object permission) {
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
            return Arrays.asList(p);
        }

        throw new IllegalArgumentException("Unsupported permission: " + permission);
    }

    private boolean isClassUserGroupManagement(String objectClass) {
        return (objectClass.equals(Group.class.getName())) || (objectClass.equals(GroupUser.class.getName()))
                || (objectClass.equals(User.class.getName()));
    }

    private boolean isClassProjectExperimentJob(String objectClass) {
        return (objectClass.equals(Project.class.getName())) || (objectClass.equals(Experiment.class.getName()))
                || (objectClass.equals(Job.class.getName()));
    }

    private boolean isExceptionClass(String objectClass) {
        return (objectClass.equals(Tile.class.getName())) || (objectClass.equals(Shortcut.class.getName()));
    }
}
