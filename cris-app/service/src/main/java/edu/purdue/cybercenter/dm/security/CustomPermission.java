/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

/**
 *
 * @author Ihsan
 */
public class CustomPermission extends BasePermission {

    private static final long serialVersionUID = 1L;

    public static final Permission EXECUTE = new CustomPermission(1 << 16, 'E'); // 65,536
    public static final Permission OWNER = new CustomPermission(1 << 17, 'O'); // 131,072

    protected CustomPermission(int mask) {
        super(mask);
    }

    protected CustomPermission(int mask, char code) {
        super(mask, code);
    }
}
