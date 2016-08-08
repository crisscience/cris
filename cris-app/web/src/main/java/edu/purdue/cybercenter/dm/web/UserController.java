package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Configuration;
import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.DomainObjectUtils;
import edu.purdue.cybercenter.dm.web.util.WebJsonHelper;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.CommunicationException;
import org.springframework.ldap.InvalidAttributeValueException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/users")
@Controller
public class UserController {

    @Autowired
    private LdapTemplate ldapTemplate;
    @Autowired
    private WebJsonHelper WebJsonHelper;

    @RequestMapping(value="/index", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("filter", request.getParameter("filter"));
        return "users/index";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object showJson(@PathVariable("id") Integer id, HttpServletRequest request, HttpServletResponse response) {
        return WebJsonHelper.show(id, request, response, User.class);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String listJson(HttpServletRequest request, HttpServletResponse response) {
        Session session = DomainObjectHelper.getHbmSession();

        Filter userInGroupFilter = null;
        Filter userNotInGroupFilter = null;
        if (request.getParameter("groupId") != null) {
            Integer groupId = Integer.parseInt(request.getParameter("groupId"));
            if (!request.getParameter("groupId").startsWith("-")) {
                userInGroupFilter = session.enableFilter("userInGroupFilter");
                userInGroupFilter.setParameter("groupId", groupId);
            } else {
                userNotInGroupFilter = session.enableFilter("userNotInGroupFilter");
                userNotInGroupFilter.setParameter("groupId", -groupId);
            }
        }

        Filter enabledFilter = null;
        if (request.getParameter("enabled") != null) {
            Boolean enabled = Boolean.parseBoolean(request.getParameter("enabled"));
            enabledFilter = session.enableFilter("enabledFilter");
            enabledFilter.setParameter("enabled", enabled);
        }

        String result = WebJsonHelper.list(request, response, User.class);

        if (enabledFilter != null) {
            session.disableFilter("enabledFilter");
        }
        if (userNotInGroupFilter != null) {
            session.disableFilter("userNotInGroupFilter");
        }
        if (userInGroupFilter != null) {
            session.disableFilter("userInGroupFilter");
        }

        return result;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> createFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) throws NamingException {
        User user = DomainObjectUtils.fromJson(json, request.getContextPath(), User.class);

        String username = user.getUsername();
        if (StringUtils.isBlank(username)) {
            // error: no username
            throw new CrisControllerException("missing username", HttpStatus.BAD_REQUEST);
        }

        int index = username.indexOf("@");
        if (index != -1) {
            // error: should no have the @purdue.edu part
            throw new CrisControllerException("wrong username please remove: " + username.substring(index), HttpStatus.BAD_REQUEST);
        }

        User existingUser = User.findByUsername(username);
        if (existingUser != null) {
            // user already exists
            throw new CrisControllerException("username already exists: " + username, HttpStatus.BAD_REQUEST);
        }

        createUser(user);

        ResponseEntity<User> responseEntity = new ResponseEntity<>(user, HttpStatus.CREATED);

        return responseEntity;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Object updateFromJson(@RequestBody String json, HttpServletRequest request, HttpServletResponse response) {
        User user = DomainObjectUtils.fromJson(json, request.getContextPath(), User.class);
        user = User.findUser(user.getId());
        if (Constant.AdminUsername.equals(user.getUsername())) {
            throw new RuntimeException("you cannot make changes to user: " + Constant.AdminUsername);
        }

        Object result;
        try {
            result = WebJsonHelper.update(json, request, response, User.class);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());
            error.put("status", "Unable to update the user");

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", error);

            result = errorResult;
        }

        return result;
    }

    private User createUser(User user) {
        String username = user.getUsername();
        Attributes attributes;
        try {
            DirContextAdapter dca = (DirContextAdapter) ldapTemplate.lookup("uid=" + user.getUsername() + ",ou=authorize,dc=purdue,dc=edu");
            attributes = dca.getAttributes();
        } catch (CommunicationException ex) {
            throw new CrisControllerException("Unable to connect to login server", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InvalidAttributeValueException ex) {
            throw new CrisControllerException("There was an error when trying to add user. Please check the login: " + username, HttpStatus.BAD_REQUEST);
        }

        String puid = "";
        String givenName = "";
        String sn = "";
        try {
            puid = "00" + (String) attributes.get("puid").get();
            givenName = (String) attributes.get("givenName").get();
            sn = (String) attributes.get("sn").get();
        } catch (NamingException ex) {
            throw new CrisControllerException("There was an error when trying to add user. Please check the login: " + username, HttpStatus.BAD_REQUEST);
        }

        // first time login
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
        user.persist();

        return user;
    }

}
