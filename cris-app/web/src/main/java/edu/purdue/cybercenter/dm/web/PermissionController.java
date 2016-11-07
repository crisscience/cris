package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.security.CustomPermission;
import edu.purdue.cybercenter.dm.util.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    private static final String EDU_PURDUE_CYBERCENTER_DM_DOMAIN = "edu.purdue.cybercenter.dm.domain.";

    private static final String CREATE = "create";
    private static final String READ = "read";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";
    private static final String EXECUTE = "execute";
    private static final String OWNER = "owner";

    private static final String S_ID = "sId";
    private static final String IS_GROUP = "group";
    private static final String USER_ID = "userId";
    private static final String GROUP_ID = "groupId";
    private static final String OBJECT_CLASS = "objectClass";
    private static final String OBJECT_ID = "objectId";
    private static final String OBJECT_IDS = "objectIds";

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String USER = "user";
    private static final String GROUP = "group";
    private static final String PERMISSION = "permission";
    private static final String INHERIT_FROM_GROUP = "inheritFromGroup";

    @Autowired
    private JdbcMutableAclService aclService;

    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(HttpServletRequest request, HttpServletResponse response) {
        return "permissions/index";
    }

    @Transactional
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getPermissions(HttpServletRequest request, HttpServletResponse response) {
        String sSid = request.getParameter(S_ID);
        String sGroup = request.getParameter(IS_GROUP);
        String sObjectClass = request.getParameter(OBJECT_CLASS);
        String sOids = request.getParameter(OBJECT_IDS);

        // if sSid is missing, use the current user
        if (StringUtils.isEmpty(sSid)) {
            sSid = ((Integer) request.getSession().getAttribute(USER_ID)).toString();
            sGroup = Boolean.FALSE.toString();
        }

        // if group is missing, assume "false"
        if (StringUtils.isEmpty(sGroup)) {
            sGroup = Boolean.FALSE.toString();
        }

        String objectClass = EDU_PURDUE_CYBERCENTER_DM_DOMAIN + sObjectClass;

        StringTokenizer st = new StringTokenizer(sOids, ",");
        List<Long> oids = new ArrayList<>();
        while (st.hasMoreElements()) {
            oids.add(Long.parseLong((String) st.nextElement()));
        }

        Sid sid;
        boolean isAdmin;
        List<Sid> sids = new ArrayList<>();
        PrincipalSid userSid = null;
        if (sGroup.equals(Boolean.FALSE.toString())) {
            sid = new PrincipalSid(sSid);
            userSid = (PrincipalSid) sid;
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
        for (Long objectId : oids) {

            // There are 4 cases
            // 1. admin: permit all
            // 2. the object exists and a permission entry for the particular user exists: use permission from the security table.
            // 3. the object exists but a permission entry for the particular user does not exist: use default, .i.e deny all
            // 4. the object does not exist:  use default, .i.e deny all
            Boolean read, update, delete, create, execute, owner;
            boolean inheritFromGroup = true;
            if (isAdmin) {
                read = true; update = true; delete = true; create = true; execute = true; owner = true;
            } else {
                read = null; update = null; delete = null; create = null; execute = null; owner = null;
                try {
                    ObjectIdentity oId = new ObjectIdentityImpl(objectClass, objectId);
                    MutableAcl acl = (MutableAcl) aclService.readAclById(oId, sids);
                    List<AccessControlEntry> aces = acl.getEntries();
                    for (AccessControlEntry ace : aces) {
                        if (ace.getSid().equals(userSid)) {
                            // if user permission explicitly assigned then use it
                            Integer permissionMask = ace.getPermission().getMask();
                            read = (permissionMask & CustomPermission.READ.getMask()) != 0;
                            update = (permissionMask & CustomPermission.WRITE.getMask()) != 0;
                            create = (permissionMask & CustomPermission.CREATE.getMask()) != 0;
                            delete = (permissionMask & CustomPermission.DELETE.getMask()) != 0;
                            execute = (permissionMask & CustomPermission.EXECUTE.getMask()) != 0;
                            owner = (permissionMask & CustomPermission.OWNER.getMask()) != 0;
                            inheritFromGroup = false;
                            break;
                        }
                        if (sids.contains(ace.getSid())) {
                            // otherwise it will be the sum of all its groups
                            if (read == null) {
                                read = false; update = false; delete = false; create = false; execute = false; owner = false;
                            }
                            Integer permissionMask = ace.getPermission().getMask();
                            read = read | (permissionMask & CustomPermission.READ.getMask()) != 0;
                            update = update | (permissionMask & CustomPermission.WRITE.getMask()) != 0;
                            create = create | (permissionMask & CustomPermission.CREATE.getMask()) != 0;
                            delete = delete | (permissionMask & CustomPermission.DELETE.getMask()) != 0;
                            execute = execute | (permissionMask & CustomPermission.EXECUTE.getMask()) != 0;
                            owner = owner | (permissionMask & CustomPermission.OWNER.getMask()) != 0;
                        }
                    }
                } catch (NotFoundException nfe) {
                    // this implies that the object does not exist
                }
            }

            Map<String, Object> objectPermission = new HashMap<>();
            objectPermission.put(READ, read);
            objectPermission.put(UPDATE, update);
            objectPermission.put(CREATE, create);
            objectPermission.put(DELETE, delete);
            objectPermission.put(EXECUTE, execute);
            objectPermission.put(OWNER, owner);
            objectPermission.put(INHERIT_FROM_GROUP, inheritFromGroup);

            String sFormat = request.getParameter("format");
            if ("old".equals(sFormat)) {
                objectPermission.put("id", objectId);
            }
            objectPermissions.put(objectId, objectPermission);
        }

        return Helper.serialize(objectPermissions);
    }

    @Transactional
    @RequestMapping(value="/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getUserPermissions(HttpServletRequest request, HttpServletResponse response) {
        String sGroupId = request.getParameter(GROUP_ID);
        String sObjectClass = request.getParameter(OBJECT_CLASS);
        String sOid = request.getParameter(OBJECT_ID);

        Group group = Group.findGroup(Integer.parseInt(sGroupId));
        Set<User> users = group.getUsers();
        List<Sid> sids = new ArrayList<>();
        users.stream().map((user) -> new PrincipalSid(user.getId().toString())).forEach((sid) -> {
            sids.add(sid);
        });

        ObjectIdentity oId = new ObjectIdentityImpl(EDU_PURDUE_CYBERCENTER_DM_DOMAIN + sObjectClass, Integer.parseInt(sOid));

        List<Map<String, Object>> userPermissions = new ArrayList<>();
        List<Integer> usersWithPermission = new ArrayList<>();
        if (!sids.isEmpty()) {
            try {
                MutableAcl acl = (MutableAcl) aclService.readAclById(oId, sids);
                List<AccessControlEntry> aces = acl.getEntries();
                aces.stream().forEach((ace) -> {
                    if (sids.contains(ace.getSid())) {
                        Integer userId = Integer.parseInt(((PrincipalSid) ace.getSid()).getPrincipal());
                        usersWithPermission.add(userId);
                        User user = User.findUser(userId);
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put(ID, userId);
                        userMap.put(FIRST_NAME, user.getFirstName());
                        userMap.put(LAST_NAME, user.getLastName());
                        Map<String, Object> userPermission = new HashMap<>();
                        userPermission.put(USER, userMap);
                        userPermission.put(PERMISSION, toPermissionMap(ace.getPermission()));
                        userPermission.put(INHERIT_FROM_GROUP, false);
                        userPermissions.add(userPermission);
                    }
                });
            } catch (NotFoundException ex) {
                // ok without acl
                System.out.println(ex.getMessage());
            }
        }

        users.stream().forEach((user) -> {
            Integer userId = user.getId();
            if (!usersWithPermission.contains(userId)) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put(ID, userId);
                userMap.put(FIRST_NAME, user.getFirstName());
                userMap.put(LAST_NAME, user.getLastName());
                Map<String, Object> userPermission = new HashMap<>();
                userPermission.put(USER, userMap);
                userPermission.put(PERMISSION, toPermissionMap(null));
                userPermission.put(INHERIT_FROM_GROUP, true);
                userPermissions.add(userPermission);
            }
        });

        return Helper.serialize(userPermissions);
    }

    @Transactional
    @RequestMapping(value="groups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public String getGroupPermissions(HttpServletRequest request, HttpServletResponse response) {
        String sUserId = request.getParameter(USER_ID);
        String sObjectClass = request.getParameter(OBJECT_CLASS);
        String sOid = request.getParameter(OBJECT_ID);

        User user = User.findUser(Integer.parseInt(sUserId));
        List<Group> groups = user.getMemberGroups();
        List<Sid> sids = new ArrayList<>();
        groups.stream().map((group) -> new GrantedAuthoritySid(group.getId().toString())).forEach((sid) -> {
            sids.add(sid);
        });

        ObjectIdentity oId = new ObjectIdentityImpl(EDU_PURDUE_CYBERCENTER_DM_DOMAIN + sObjectClass, Integer.parseInt(sOid));

        List<Map<String, Map>> groupPermissions = new ArrayList<>();
        if (!sids.isEmpty()) {
            try {
                MutableAcl acl = (MutableAcl) aclService.readAclById(oId, sids);
                List<AccessControlEntry> aces = acl.getEntries();
                aces.stream().forEach((ace) -> {
                    if (sids.contains(ace.getSid())) {
                        Integer groupId = Integer.parseInt(((GrantedAuthoritySid) ace.getSid()).getGrantedAuthority());
                        Group group = Group.findGroup(groupId);
                        Map<String, Object> groupMap = new HashMap<>();
                        groupMap.put(ID, groupId);
                        groupMap.put(NAME, group.getName());
                        Map<String, Map> groupPermission = new HashMap<>();
                        groupPermission.put(GROUP, groupMap);
                        groupPermission.put(PERMISSION, toPermissionMap(ace.getPermission()));
                        groupPermissions.add(groupPermission);
                    }
                });
            } catch (NotFoundException ex) {
                // ok without acl
                System.out.println(ex.getMessage());
            }
        }

        return Helper.serialize(groupPermissions);
    }

    @Transactional
    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object savePermissions(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = User.findByUsername(username);
        boolean currentUserIsAdmin = currentUser.isAdmin();

        Map<String, Object> object = Helper.deserialize(json, Map.class);

        String sSid = object.get(S_ID) != null ? object.get(S_ID).toString() : "";
        Boolean isGroup = (Boolean) object.get(IS_GROUP);
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

        String objectClass = EDU_PURDUE_CYBERCENTER_DM_DOMAIN + (String) object.get(OBJECT_CLASS);
        Long objectId = ((Integer) object.get(OBJECT_ID)).longValue();

        if (objectId == 0 && !currentUserIsAdmin) {
            return new ResponseEntity<>("{\"message\": \"Permission Not Created\"}", HttpStatus.FORBIDDEN);
        }

        CumulativePermission cPermission = new CumulativePermission();
        cPermission.clear();
        boolean bit = (boolean) object.get(READ);
        if (bit) {
            cPermission.set(CustomPermission.READ);
        }
        bit = (boolean) object.get(UPDATE);
        if (bit) {
            cPermission.set(CustomPermission.WRITE);
        }
        bit = (boolean) object.get(CREATE);
        if (bit) {
            cPermission.set(CustomPermission.CREATE);
        }
        bit = (boolean) object.get(DELETE);
        if (bit) {
            cPermission.set(CustomPermission.DELETE);
        }
        if (objectId != 0) {
            bit = (boolean) object.get(EXECUTE);
            if (bit) {
                cPermission.set(CustomPermission.EXECUTE);
            }
            bit = (boolean) object.get(OWNER);
            if (bit) {
                cPermission.set(CustomPermission.OWNER);
            }
        } else {
            cPermission.clear(CustomPermission.EXECUTE);
            cPermission.clear(CustomPermission.OWNER);
        }

        ObjectIdentity objectIdentity = new ObjectIdentityImpl(objectClass, objectId);
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
                        break;
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

    @Transactional
    @RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object deletePermissions(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException {
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = User.findByUsername(username);
        boolean currentUserIsAdmin = currentUser.isAdmin();

        Map<String, Object> object = Helper.deserialize(json, Map.class);

        String sSid = object.get(S_ID).toString();
        Boolean isGroup = (Boolean) object.get(IS_GROUP);
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

        String objectClass = EDU_PURDUE_CYBERCENTER_DM_DOMAIN + (String) object.get(OBJECT_CLASS);
        Long objectId = ((Integer) object.get(OBJECT_ID)).longValue();
        ObjectIdentity objectIdentity = new ObjectIdentityImpl(objectClass, objectId);
        try {
            MutableAcl acl = (MutableAcl) aclService.readAclById(objectIdentity, sids);
            List<AccessControlEntry> aces = acl.getEntries();
            for (int i = 0; i < aces.size(); i++) {
                AccessControlEntry ace = acl.getEntries().get(i);
                if (ace.getSid().equals(sid)) {
                    acl.deleteAce(i);
                    aclService.updateAcl(acl);
                    break;
                }
            }
        } catch (NotFoundException nfe) {
            // object does not exist
        }

        return new ResponseEntity<>("{\"message\": \"Permission Successfully deleted\"}", HttpStatus.NO_CONTENT);
    }

    private Map<String, Boolean> toPermissionMap(Permission permission) {
        Map<String, Boolean> permissionMap = new HashMap<>();

        if (permission != null) {
            Integer permissionMask = permission.getMask();
            permissionMap.put(READ, (permissionMask & CustomPermission.READ.getMask()) != 0);
            permissionMap.put(UPDATE, (permissionMask & CustomPermission.WRITE.getMask()) != 0);
            permissionMap.put(CREATE, (permissionMask & CustomPermission.CREATE.getMask()) != 0);
            permissionMap.put(DELETE, (permissionMask & CustomPermission.DELETE.getMask()) != 0);
            permissionMap.put(EXECUTE, (permissionMask & CustomPermission.EXECUTE.getMask()) != 0);
            permissionMap.put(OWNER, (permissionMask & CustomPermission.OWNER.getMask()) != 0);
        } else {
            permissionMap.put(READ, null);
            permissionMap.put(UPDATE, null);
            permissionMap.put(CREATE, null);
            permissionMap.put(DELETE, null);
            permissionMap.put(EXECUTE, null);
            permissionMap.put(OWNER, null);
        }

        return permissionMap;
    }
}
