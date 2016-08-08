package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Configuration;
import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.GroupUser;
import edu.purdue.cybercenter.dm.domain.Tenant;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.SecurityHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/tenants")
@Controller
public class TenantController {

    static final private String PublicPassword = "d41d62b0-3cbc-11e2-a25f-0800200c9a66";
    static final private int MinPasswordLength = 8;
    static final private int MaxNameLength = 80;

    @RequestMapping(value = "/createForm", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String createForm() {
        return "tenants/create";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    public String create(Model model, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> errors = new HashMap<>();

        String urlIdentifier = request.getParameter("urlIdentifier");
        String name = request.getParameter("name");
        String password1 = request.getParameter("password1");
        String password2 = request.getParameter("password2");

        if (password1 == null || password1.isEmpty()|| password2 == null || password2.isEmpty()) {
            errors.put("password", "password is missing");
        } else if (!password1.equals(password2)) {
            errors.put("password", "passwords do not macth");
        } else if (password1.length() < MinPasswordLength) {
            errors.put("password", "password too short: must be at least " + MinPasswordLength + " characters long");
        }

        if (name == null || name.isEmpty()) {
            errors.put("name", "no name specified");
        } else if (name.length() > MaxNameLength) {
            errors.put("name", "name too long: " + MaxNameLength + " characters maximum");
        }

        if (urlIdentifier == null || urlIdentifier.isEmpty()) {
            errors.put("urlIdentifier", "URL identifier is missing");
        } else if (!urlIdentifier.matches("^[a-z_]+$")) {
            errors.put("urlIdentifier", "URL identifier can only consist of lower case letters and underscores");
        } else {
            Query query = DomainObjectHelper.createNamedQuery("Tenant.findByUrlIdentifier").setParameter("urlIdentifier", urlIdentifier);
            try {
                Tenant tenant = (Tenant) query.getSingleResult();
                if (tenant != null) {
                    errors.put("urlIdentifier", "this workspace URL identifier has been taken: " + urlIdentifier);
                }
            } catch (Exception ex) {
                // expected
            }
        }

        model.addAttribute("urlIdentifier", urlIdentifier);
        model.addAttribute("name", name);
        model.addAttribute("errors", errors);

        if (errors.isEmpty()) {
            ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);

            /*
             * Everything is cool and we'll create a new workspave
             */
            Tenant tenant = new Tenant();
            tenant.setUuid(UUID.randomUUID());
            tenant.setName(name);
            tenant.setUrlIdentifier(urlIdentifier);
            tenant.setEnabled(true);
            tenant.persist();

            // impersonate the tenant and current user (null)
            edu.purdue.cybercenter.dm.threadlocal.TenantId.set(tenant.getId());
            edu.purdue.cybercenter.dm.threadlocal.UserId.set(null);

            /*
             * admin for the workspace
             */
            User adminUser = new User();

            String salt = SecurityHelper.generateSalt(encoder);

            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setUsername(Constant.AdminUsername);
            adminUser.setSalt(salt);
            adminUser.setPassword(encoder.encodePassword(password2, salt));

            adminUser.setAccountNonExpired(true);
            adminUser.setAccountNonLocked(true);
            adminUser.setCredentialsNonExpired(true);
            adminUser.setEnabled(true);

            adminUser.persist();

            /*
             * admin group
             */
            Group adminGroup = new Group();
            adminGroup.setName(Constant.AdminGroupName);
            adminGroup.setEnabled(true);
            adminGroup.persist();

            /*
             * add the user to admin group
             */
            GroupUser groupUser = new GroupUser();
            groupUser.setGroupId(adminGroup);
            groupUser.setUserId(adminUser);
            groupUser.persist();

            /*
             * and public user for the workspace
             */
            User publicUser = new User();

            salt = SecurityHelper.generateSalt(encoder);

            publicUser.setFirstName("Public");
            publicUser.setLastName("User");
            publicUser.setUsername(Constant.PublicUsername);
            publicUser.setSalt(salt);
            publicUser.setPassword(encoder.encodePassword(PublicPassword, salt));

            publicUser.setAccountNonExpired(true);
            publicUser.setAccountNonLocked(true);
            publicUser.setCredentialsNonExpired(true);
            publicUser.setEnabled(true);

            publicUser.persist();

            /*
             * Configuration
             */
            Configuration configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("externalSource");
            configuration.setValueText("Purdue University");
            configuration.persist();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("CopyrightYear");
            configuration.setValueText(dateFormat.format(new Date()));
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsFavicon");
            configuration.setValueText("favicon.ico");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsBannerImage");
            configuration.setValueText("header.jpg");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsAuthBackgroundImage");
            configuration.setValueText("");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsName");
            configuration.setValueText(name);
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsDescription");
            configuration.setValueText("");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsSigninInstruction");
            configuration.setValueText("");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsSignupInstruction");
            configuration.setValueText("");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsAuthProblem");
            configuration.setValueText("");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsAuthReset");
            configuration.setValueText("");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsEmailGeneral");
            configuration.setValueText("");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsEmailAccountProblem");
            configuration.setValueText("");
            configuration.persist();

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsHomeUri");
            configuration.setValueText("/home");
            configuration.persist();

            // reset tenant id to null
            edu.purdue.cybercenter.dm.threadlocal.TenantId.set(null);
            edu.purdue.cybercenter.dm.threadlocal.UserId.set(null);

            return "redirect:/tenants/show";
        } else {
            return "tenants/create";
        }
    }

    @RequestMapping(value = "/show", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String show(Model model, HttpServletRequest request, HttpServletResponse response) {
        String urlIdentifier = request.getParameter("urlIdentifier");
        String name = request.getParameter("name");
        model.addAttribute("urlIdentifier", urlIdentifier);
        model.addAttribute("name", name);
        return "tenants/show";
    }
}
