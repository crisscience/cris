/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.domain.SidRetrievalStrategyImpl;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Ihsan
 */
public class CustomSidRetrievalStrategyImpl extends SidRetrievalStrategyImpl {

    public CustomSidRetrievalStrategyImpl() {
        super();
    }

    public CustomSidRetrievalStrategyImpl(RoleHierarchy roleHierarchy) {
        super(roleHierarchy);
    }

    @Override
    public List<Sid> getSids(Authentication authentication) {

        Object principal = authentication.getPrincipal();
        User user = ((UserDetailsAdapter) principal).getUser();
        List<Group> groups = user.getMemberGroups();

        List<Sid> sids = new ArrayList<>(groups.size() + 1);
        sids.add(new PrincipalSid(user.getId().toString()));

        for (Group group : groups) {
            sids.add(new GrantedAuthoritySid(group.getId().toString()));
        }

        return sids;
    }
}
