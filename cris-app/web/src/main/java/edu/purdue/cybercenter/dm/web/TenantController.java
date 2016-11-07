package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Configuration;
import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.GroupUser;
import edu.purdue.cybercenter.dm.domain.Tenant;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.repository.ConfigurationRepository;
import edu.purdue.cybercenter.dm.repository.GroupRepository;
import edu.purdue.cybercenter.dm.repository.GroupUserRepository;
import edu.purdue.cybercenter.dm.repository.TenantRepository;
import edu.purdue.cybercenter.dm.repository.UserRepository;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.SecurityHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/tenants")
@Controller
public class TenantController {

    static final private String PUBLIC_PASSWORD = "d41d62b0-3cbc-11e2-a25f-0800200c9a66";
    static final private int MIN_PASSWORD_LENGTH = 8;
    static final private int MAX_PASSWORD_LENGTH = 80;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupUserRepository groupUserRepository;
    @Autowired
    private ConfigurationRepository configurationRepository;

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
        } else if (password1.length() < MIN_PASSWORD_LENGTH) {
            errors.put("password", "password too short: must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        if (name == null || name.isEmpty()) {
            errors.put("name", "no name specified");
        } else if (name.length() > MAX_PASSWORD_LENGTH) {
            errors.put("name", "name too long: " + MAX_PASSWORD_LENGTH + " characters maximum");
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
            tenant = tenantRepository.save(tenant);

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

            adminUser = userRepository.save(adminUser);

            /*
             * admin group
             */
            Group adminGroup = new Group();
            adminGroup.setName(Constant.AdminGroupName);
            adminGroup.setEnabled(true);
            groupRepository.save(adminGroup);

            /*
             * add the user to admin group
             */
            GroupUser groupUser = new GroupUser();
            groupUser.setGroupId(adminGroup);
            groupUser.setUserId(adminUser);
            groupUserRepository.save(groupUser);

            /*
             * and public user for the workspace
             */
            User publicUser = new User();

            salt = SecurityHelper.generateSalt(encoder);

            publicUser.setFirstName("Public");
            publicUser.setLastName("User");
            publicUser.setUsername(Constant.PublicUsername);
            publicUser.setSalt(salt);
            publicUser.setPassword(encoder.encodePassword(PUBLIC_PASSWORD, salt));

            publicUser.setAccountNonExpired(true);
            publicUser.setAccountNonLocked(true);
            publicUser.setCredentialsNonExpired(true);
            publicUser.setEnabled(true);

            userRepository.save(publicUser);

            /*
             * Configuration
             */
            List<Configuration> configurations = new ArrayList<>();
            Configuration configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("externalSource");
            configuration.setValueText("Purdue University");
            configurations.add(configuration);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("CopyrightYear");
            configuration.setValueText(dateFormat.format(new Date()));
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsFavicon");
            configuration.setValueText("favicon.ico");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsBannerImage");
            configuration.setValueText("header.jpg");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsAuthBackgroundImage");
            configuration.setValueText("");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsName");
            configuration.setValueText(name);
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsDescription");
            configuration.setValueText("");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsSigninInstruction");
            configuration.setValueText("If you are a Purdue user, please use your Purdue Career Account.");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsSignupInstruction");
            configuration.setValueText("If you are a Purdue user, please use your Purdue Career Account. If outside of Purdue, create a new account here.");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsAuthProblem");
            configuration.setValueText("Please provide the email that you used as username. Password reset instruction will be sent to this email.");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsAuthReset");
            configuration.setValueText("Please copy/paste the token in the email that has been sent to your, then enter a new password.");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsEmailGeneral");
            configuration.setValueText("cyber@purdue.edu");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsEmailAccountProblem");
            configuration.setValueText("cyber@purdue.edu");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsHomeUri");
            configuration.setValueText("/home");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsAuthFailedGeneral");
            configuration.setValueText("");
            configurations.add(configuration);

            configuration = new Configuration();
            configuration.setType("text");
            configuration.setName("wsAuthFailedDisabled");
            configuration.setValueText("");
            configurations.add(configuration);

            configurationRepository.save(configurations);

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
