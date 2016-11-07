/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityRetrievalStrategyImpl;
import org.springframework.security.acls.domain.PermissionFactory;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.ObjectIdentityGenerator;
import org.springframework.security.acls.model.ObjectIdentityRetrievalStrategy;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Ihsan
 */
public class CustomAclPermissionEvaluator extends AclPermissionEvaluator {

    private final AclService aclService;
    private final PermissionFactory permissionFactory = new CustomPermissionFactory();
    private final ObjectIdentityGenerator objectIdentityGenerator = new ObjectIdentityRetrievalStrategyImpl();
    private final ObjectIdentityRetrievalStrategy objectIdentityRetrievalStrategy = new ObjectIdentityRetrievalStrategyImpl();

    public CustomAclPermissionEvaluator(AclService aclService) {
        super(aclService);
        this.aclService = aclService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object domainObject, Object permission) {
        if (domainObject == null) {
            return false;
        }

        ObjectIdentity objectIdentity = objectIdentityRetrievalStrategy.getObjectIdentity(domainObject);

        return checkPermission(authentication, objectIdentity, permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        ObjectIdentity objectIdentity = objectIdentityGenerator.createObjectIdentity(targetId, targetType);

        return checkPermission(authentication, objectIdentity, permission);
    }

    private boolean checkPermission(Authentication authentication, ObjectIdentity oid, Object permission) {
        Permission requiredPermission = toPermission(permission);
        if (requiredPermission.getMask() == 0) {
            return false;
        }

        // Obtain the SIDs applicable to the principal
        Object principal = authentication.getPrincipal();
        User user = ((UserDetailsAdapter) principal).getUser();
        Sid userSid = new PrincipalSid(user.getId().toString());

        boolean isGranted = false;
        boolean checkGroups = false;
        try {
            // Lookup only ACLs for userSid
            Acl acl = aclService.readAclById(oid, Arrays.asList(userSid));
            try {
                isGranted = acl.isGranted(Arrays.asList(requiredPermission), Arrays.asList(userSid), false);
            } catch (NotFoundException nfe1) {
                checkGroups = true;
                List<AccessControlEntry> aces = acl.getEntries();
                for (AccessControlEntry ace : aces) {
                    if (userSid.equals(ace.getSid())) {
                        checkGroups = false;
                    }
                }
            }
        } catch (NotFoundException nfe) {
            // there's no permission on userSid, check groupSids
            checkGroups = true;
        }

        if (checkGroups) {
            List<Sid> groupSids = getGroupSids(user.getId());
            if (!groupSids.isEmpty()) {
                try {
                    Acl acl = aclService.readAclById(oid, groupSids);
                    isGranted = acl.isGranted(Arrays.asList(requiredPermission), groupSids, false);
                } catch (NotFoundException nfe) {
                    isGranted = false;
                }
            }
        }

        return isGranted;
    }

    private List<Sid> getGroupSids(int userId) {
        User user = User.findUser(userId);
        List<Group> groups = user.getMemberGroups();

        List<Sid> sids = new ArrayList<>(groups.size());
        groups.stream().forEach((group) -> {
            sids.add(new GrantedAuthoritySid(group.getId().toString()));
        });

        return sids;
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
}
