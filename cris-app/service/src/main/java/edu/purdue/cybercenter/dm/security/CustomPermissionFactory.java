/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import java.util.Map;
import org.springframework.security.acls.domain.DefaultPermissionFactory;
import org.springframework.security.acls.model.Permission;

/**
 *
 * @author Ihsan
 */
public class CustomPermissionFactory extends DefaultPermissionFactory {

    public CustomPermissionFactory() {
        registerPublicPermissions(CustomPermission.class);
    }

    public CustomPermissionFactory(Class<? extends Permission> permissionClass) {
        super(permissionClass);
    }

    public CustomPermissionFactory(Map<String, ? extends Permission> namedPermissions) {
        super(namedPermissions);
    }
}
