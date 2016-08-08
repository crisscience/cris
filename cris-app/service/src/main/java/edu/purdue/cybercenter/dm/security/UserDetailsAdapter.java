/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import edu.purdue.cybercenter.dm.domain.Permission;
import edu.purdue.cybercenter.dm.domain.User;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author xu222
 */
public class UserDetailsAdapter implements UserDetails {

    private User user;

    public UserDetailsAdapter() {
    }

    public UserDetailsAdapter(User user) {
        this.user = user;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> gas = new HashSet<>();
        List<Permission> permissions = Permission.findRoleTypeId(user);
        for (Permission permission : permissions) {
            GrantedAuthority ga = new SimpleGrantedAuthority(permission.getRoleTypeId().getName());
            if (!gas.contains(ga)) {
                gas.add(ga);
            }
        }

        return gas;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    public String getSalt() {
        return user.getSalt();
    }

    public User getUser() {
        return user;
    }
}
