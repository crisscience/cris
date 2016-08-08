/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import edu.purdue.cybercenter.dm.domain.Configuration;
import edu.purdue.cybercenter.dm.domain.User;
import java.util.Collection;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import org.springframework.dao.DataAccessException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 *
 * @author xu222
 */
public class UserDetailsServiceImpl implements UserDetailsService, UserDetailsContextMapper {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        User user = User.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("username not found: " + username);
        }

        UserDetailsAdapter ud = new UserDetailsAdapter(user);
        return ud;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authority) {
        Attributes attributes = ctx.getAttributes();

        User user = null;
        String puid = "";
        String givenName = "";
        String sn = "";
        try {
            puid = "00" + (String) attributes.get("puid").get();
            givenName = (String) attributes.get("givenName").get();
            sn = (String) attributes.get("sn").get();
            user = User.findByExternal("Purdue University", puid);
        } catch (NamingException ex) {
            throw new RuntimeException("Ldap authentication failed: " + ex.getMessage());
        }

        if (user == null) {
            // first time login
            user = new User();
            user.setExternalSource(Configuration.findProperty("externalSource"));
            user.setExternalId(puid);
            user.setUsername(username);
            user.setPassword("UNKNOWN");
            user.setSalt("UNKNOWN");
            user.setFirstName(givenName);
            user.setLastName(sn);
            user.setEmail(username + "@purdue.edu");
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            user.setEnabled(false);

            user.persist();
            throw new DisabledException("Account is disabled: " + username);
        }

        // existing user
        UserDetailsAdapter uda = new UserDetailsAdapter(user);

        return uda;
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
