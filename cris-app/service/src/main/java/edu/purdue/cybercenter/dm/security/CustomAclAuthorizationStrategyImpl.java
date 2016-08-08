/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.User;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.AclAuthorizationStrategyImpl;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.acls.model.SidRetrievalStrategy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Ihsan
 */
public class CustomAclAuthorizationStrategyImpl extends AclAuthorizationStrategyImpl {

    private final SidRetrievalStrategy sidRetrievalStrategy = new CustomSidRetrievalStrategyImpl();

    public CustomAclAuthorizationStrategyImpl(GrantedAuthority... auths) {
        super(auths);
    }

    @Override
    public void securityCheck(Acl acl, int changeType) {
        if ((SecurityContextHolder.getContext() == null)
                || (SecurityContextHolder.getContext().getAuthentication() == null)
                || !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            throw new AccessDeniedException("Authenticated principal required to operate with ACLs");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Check if authorized by virtue of ACL ownership
        Sid currentUser = new PrincipalSid(authentication);

        if (currentUser.equals(acl.getOwner())
                && ((changeType == CHANGE_GENERAL) || (changeType == CHANGE_OWNERSHIP))) {
            return;
        }

        // Not authorized by ACL ownership; try via admin group
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = ((UserDetailsAdapter) principal).getUser();

        List<Group> groups = Group.findGroupsByUserId(user.getId());
        for (Group group : groups) {
            if (group.getName().equals(Constant.AdminGroupName) && ((changeType == CHANGE_GENERAL) || (changeType == CHANGE_OWNERSHIP) || (changeType == CHANGE_AUDITING))) {
                return;
            }
        }

        // Try to get permission via ACEs within the ACL
        List<Sid> sids = sidRetrievalStrategy.getSids(authentication);

        if (acl.isGranted(Arrays.asList(BasePermission.ADMINISTRATION), sids, false)) {
            return;
        }

        throw new AccessDeniedException(
                "Principal does not have required ACL permissions to perform requested operation");
    }
}
