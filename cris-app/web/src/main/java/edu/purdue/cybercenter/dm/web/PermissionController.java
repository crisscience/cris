package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.security.CustomPermission;
import edu.purdue.cybercenter.dm.util.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Ihsan
 */
@RequestMapping("/permissions")
@Controller
public class PermissionController {

    @Autowired
    private JdbcMutableAclService aclService;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        User user = User.findUser((Integer) request.getSession().getAttribute("userId"));
        model.addAttribute("isAdmin", user.isAdmin());
        return "permissions/index";
    }

    @Transactional
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getPermissions(HttpServletRequest request, HttpServletResponse response) {
        String sObjectClass = request.getParameter("objectClass");
        String sOids = request.getParameter("objectIds");
        String sSid = request.getParameter("sId");
        String sGroup = request.getParameter("group");
        String sFormat = request.getParameter("format");

        // if sSid is missing, use the current user
        if (StringUtils.isEmpty(sSid)) {
            sSid = ((Integer) request.getSession().getAttribute("userId")).toString();
        }

        // if group is missing, assume "no"
        if (StringUtils.isEmpty(sGroup)) {
            sGroup = "no";
        }

        String objectClass = "edu.purdue.cybercenter.dm.domain." + sObjectClass;

        StringTokenizer st = new StringTokenizer(sOids, ",");
        List<Long> oids = new ArrayList<>();
        while (st.hasMoreElements()) {
            oids.add(Long.parseLong((String) st.nextElement()));
        }

        Sid sid;
        boolean isAdmin;
        List<Sid> sids = new ArrayList<>();
        if (sGroup.equals("no")) {
            sid = new PrincipalSid(sSid);
            User user = User.findUser(Integer.valueOf(sSid));
            isAdmin = user.isAdmin();
            sids.add(sid);

            // groups
            List<Group> groups = user.getMemberGroups();
            for (Group group : groups) {
                sid = new GrantedAuthoritySid(group.getId().toString());
                sids.add(sid);
            }
        } else {
            sid = new GrantedAuthoritySid(sSid);
            Group group = Group.findGroup(Integer.valueOf(sSid));
            isAdmin = group.isAdmin();
            sids.add(sid);
        }

        Map<Object, Map> objectPermissions = new HashMap<>();
        for (int i = 0; i < oids.size(); i++) {
            // There are 4 cases
            // 1. admin: permit all
            // 2. the object exists and a permission entry for the particular user exists: use permission from the security table.
            // 3. the object exists but a permission entry for the particular user does not exist: use default, .i.e deny all
            // 4. the object does not exist:  use default, .i.e deny all
            boolean read, update, delete, create, execute;
            Long objectId = oids.get(i);
            if (isAdmin) {
                read = true; update = true; delete = true; create = true; execute = true;
            } else {
                read = false; update = false; delete = false; create = false; execute = false;
                try {
                    ObjectIdentity oId = new ObjectIdentityImpl(objectClass, objectId);
                    MutableAcl acl = (MutableAcl) aclService.readAclById(oId, sids);
                    List<AccessControlEntry> aces = acl.getEntries();
                    for (AccessControlEntry ace : aces) {
                        if (sids.contains(ace.getSid())) {
                            Integer permissionMask = ace.getPermission().getMask();
                            read = read | (permissionMask & CustomPermission.READ.getMask()) != 0;
                            update = update | (permissionMask & CustomPermission.WRITE.getMask()) != 0;
                            create = create | (permissionMask & CustomPermission.CREATE.getMask()) != 0;
                            delete = delete | (permissionMask & CustomPermission.DELETE.getMask()) != 0;
                            execute = execute | (permissionMask & CustomPermission.EXECUTE.getMask()) != 0;
                        }
                    }
                } catch (NotFoundException nfe) {
                    // this implies that the object does not exist
                }
            }

            Map<String, Object> objectPermission = new HashMap<>();
            objectPermission.put("read", read);
            objectPermission.put("update", update);
            objectPermission.put("create", create);
            objectPermission.put("delete", delete);
            objectPermission.put("execute", execute);

            if ("old".equals(sFormat)) {
                objectPermission.put("id", objectId);
                objectPermissions.put("" + i, objectPermission);
            } else {
                objectPermissions.put(objectId, objectPermission);
            }
        }

        return Helper.serialize(objectPermissions);
    }

    @Transactional
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object savePermissions(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = User.findByUsername(username);
        boolean currentUserIsAdmin = currentUser.isAdmin();

        Map<String, Object> object = Helper.deserialize(json, Map.class);

        String sObjectClass = (String) object.get("objectClass");
        Long oid = ((Integer) object.get("objectId")).longValue();
        String sSid = object.get("sId") != null ? object.get("sId").toString() : "";
        boolean isGroup = (boolean) object.get("group");

        CumulativePermission cPermission = new CumulativePermission();
        cPermission.clear();
        boolean bit = (boolean) object.get("read");
        if (bit) {
            cPermission.set(CustomPermission.READ);
        }
        bit = (boolean) object.get("update");
        if (bit) {
            cPermission.set(CustomPermission.WRITE);
        }
        bit = (boolean) object.get("create");
        if (bit) {
            cPermission.set(CustomPermission.CREATE);
        }
        bit = (boolean) object.get("delete");
        if (bit) {
            cPermission.set(CustomPermission.DELETE);
        }
        bit = (boolean) object.get("execute");
        if (bit) {
            cPermission.set(CustomPermission.EXECUTE);
        }

        String objectClass = "edu.purdue.cybercenter.dm.domain." + sObjectClass;

        Sid sid;
        if (!isGroup) {
            sid = new PrincipalSid(sSid);
            User user = User.findUser(Integer.valueOf(sSid));
            if (user.isAdmin()) {
                return new ResponseEntity<>("{\"message\": \"The User is an Admin and cannot be assigned Permissions\"}", HttpStatus.PRECONDITION_FAILED);
            }
        } else {
            sid = new GrantedAuthoritySid(sSid);
            Group group = Group.findGroup(Integer.valueOf(sSid));
            if (group.isAdmin()) {
                return new ResponseEntity<>("{\"message\": \"The Group is an Admin Group and cannot be assigned Permissions\"}", HttpStatus.PRECONDITION_FAILED);
            }
        }
        List<Sid> sids = new ArrayList<>();
        sids.add(sid);

        if (oid == 0 && !currentUserIsAdmin) {
            return new ResponseEntity<>("{\"message\": \"Permission Not Created\"}", HttpStatus.FORBIDDEN);
        }

        ObjectIdentity objectIdentity = new ObjectIdentityImpl(objectClass, oid);
        try {
            MutableAcl acl = (MutableAcl) aclService.readAclById(objectIdentity, sids);
            List<AccessControlEntry> aces = acl.getEntries();
            boolean updated = false;
            if (aces != null && !aces.isEmpty()) {
                // This loop is to insert an update on a permission
                for (int i = 0; i < acl.getEntries().size(); i++) {
                    if (acl.getEntries().get(i).getSid().equals(sid)) {
                        acl.updateAce(i, cPermission);
                        aclService.updateAcl(acl);
                        updated = true;
                    }
                }
            }

            if (!updated) {
                // This is to insert if an object exists but permission entry for the particular user does not exist
                acl.insertAce(acl.getEntries().size(), cPermission, sid, true);
                aclService.updateAcl(acl);
            }
        } catch (NotFoundException nfe) {
            // This is if the object does not exist
            MutableAcl acl = aclService.createAcl(objectIdentity);
            acl.insertAce(acl.getEntries().size(), cPermission, sid, true);
            aclService.updateAcl(acl);
        }

        return new ResponseEntity<>("{\"message\": \"Permission Successfully Created\"}", HttpStatus.OK);
    }

}
