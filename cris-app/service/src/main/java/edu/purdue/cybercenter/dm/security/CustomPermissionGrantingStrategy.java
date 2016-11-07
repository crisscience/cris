/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.security;

import java.util.List;
import org.springframework.security.acls.domain.AuditLogger;
import org.springframework.security.acls.domain.DefaultPermissionGrantingStrategy;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

/**
 *
 * @author Ihsan
 */
public class CustomPermissionGrantingStrategy extends DefaultPermissionGrantingStrategy {

    public CustomPermissionGrantingStrategy(AuditLogger auditLogger) {
        super(auditLogger);
    }

    @Override
    public boolean isGranted(Acl acl, List<Permission> permission, List<Sid> sids, boolean administrativeMode)
            throws NotFoundException {

        final List<AccessControlEntry> aces = acl.getEntries();
        AccessControlEntry firstRejection = null;

        for (Permission p : permission) {
            for (Sid sid : sids) {
                // Attempt to find exact match for this permission mask and SID
                boolean scanNextSid = true;
                for (AccessControlEntry ace : aces) {

                    if (((ace.getPermission().getMask() & p.getMask()) == p.getMask()) && ace.getSid().equals(sid)) {
                        // Found a matching ACE, so its authorization decision will prevail
                        if (ace.isGranting()) {
                            // Success
                           return true;
                        }
                        // Failure for this permission, so stop search
                        // We will see if they have a different permission
                        // (this permission is 100% rejected for this SID)
                        if (firstRejection == null) {
                            // Store first rejection for auditing reasons
                            firstRejection = ace;
                        }
                        scanNextSid = false; // helps break the loop
                        break; // exit aces loop
                    }
                }
                if (!scanNextSid) {
                    break; // exit SID for loop (now try next permission)
                }
            }
        }
        if (firstRejection != null) {
            // We found an ACE to reject the request at this point, as no
            // other ACEs were found that granted a different permission
            return false;
        }
        // No matches have been found so far
        if (acl.isEntriesInheriting() && (acl.getParentAcl() != null)) {
            // We have a parent, so let them try to find a matching ACE
            return acl.getParentAcl().isGranted(permission, sids, false);
        } else {
            // We either have no parent, or we're the uppermost parent
            throw new NotFoundException("Unable to locate a matching ACE for passed permissions and SIDs");
        }
    }
}
