package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.Configuration;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.util.SecurityHelper;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {

    private static final int MIN_USERNAME_LENGTH = 8;
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Autowired
    private JavaMailSender mailSender;

    @RequestMapping(value = "auth/signin", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String signinForm() {
        return "auth/signin";
    }

    @RequestMapping(value = "auth/signup", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String signupForm() {
        return "auth/signup";
    }

    @RequestMapping(value = "auth/signup", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    public String signup(@RequestParam(value = "password2", required = true) String password2, @Valid User user, BindingResult result, Model model, HttpServletRequest request, HttpServletResponse response) {
        /*
         * Precondition:
         *  This should have already been checked but we'll check it again
         *  1. all the required fields are present: first name, last name, email, username, password and passowrd2
         *  2. the two passwords are the same
         */
        boolean hasError = false;
        model.addAttribute("user", user);

        if (user.getPassword() == null ? false : !user.getPassword().equals(password2)) {
            hasError = true;
            result.rejectValue("password", "", "Passwords are not the same");
        }

        if (user.getUsername() == null || !user.getUsername().equals(user.getEmail())) {
            hasError = true;
            result.rejectValue("email", "", "Emails are not the same");
        } else if (User.isExist(user.getUsername())) {
            /* check if the email/username is existed  */
            hasError = true;
            result.rejectValue("email", "", "This email has already been taken");
        }

        if (user.getUsername().length() < MIN_USERNAME_LENGTH) {
            hasError = true;
            result.rejectValue("email", "", "Email must be at lease " + MIN_USERNAME_LENGTH + " characters long");
        }

        if (user.getEmail() != null && !user.getEmail().matches("^[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")) {
            hasError = true;
            result.rejectValue("email", "", "Invalid email address");
        }

        if (user.getPassword().length() < MIN_USERNAME_LENGTH) {
            hasError = true;
            result.rejectValue("password", "", "Password must be at lease " + MIN_PASSWORD_LENGTH + " characters long");
        }

        if (result.getFieldErrorCount("email") > 0) {
            hasError = true;
        }

        if (hasError) {
            user.setPassword("");
            password2 = "";
            return "auth/signup";
        }

        /*
         * Everything is cool and we'll create a new user
         */
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);

        String salt = SecurityHelper.generateSalt(encoder);

        user.setSalt(salt.toString());
        user.setPassword(encoder.encodePassword(password2, salt));

        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(false);

        user.persist();

        return "redirect:/auth/signin";
    }

    @RequestMapping(value = "auth/problem", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String problemForm(User user, BindingResult result, @RequestParam(value = "email", required = false) String email, Model model, HttpServletRequest request, HttpServletResponse response) {
        return "auth/problem";
    }

    @RequestMapping(value = "auth/problem", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    public String problem(User user, BindingResult result, @RequestParam(value = "email", required = false) String email, Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("email", email);

        if (StringUtils.isBlank(email)) {
            result.reject("", "You must provide an email address");
            return "auth/problem";
        }

        user = User.findByEmail(email);
        if (user == null) {
            result.reject("user.email", "Email address does not exist");
            return "auth/problem";
        }

        // TODO
        // 1. record the request


        // 2. send an email with a link
        String token = UUID.randomUUID().toString();
        MimeMessage message = mailSender.createMimeMessage();
        try {
            Address address = new InternetAddress(Configuration.findProperty("wsEmailAccountProblem"));
            message.addRecipients(RecipientType.TO, user.getEmail());
            message.setFrom(address);
            message.setSubject("Password Reset Instruction");
            message.setText("Dear " + user.getFirstName() + ":\n\n" + "Please cut and paste the token into the token field: " + token + "\n\n" + "Sincerely,\n\n" + Configuration.findProperty("wsName") + " Team");
        } catch (MessagingException ex) {
            Logger.getLogger(AuthenticationController.class.getName()).log(Level.SEVERE, null, ex);
        }
        mailSender.send(message);

        // 3. add email to session
        request.getSession().setAttribute("sessionId", request.getSession().getId());
        request.getSession().setAttribute("email", email);
        request.getSession().setAttribute("token", token);

        return "redirect:/auth/reset";
    }

    @RequestMapping(value = "auth/reset", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String resetForm() {
        return "auth/reset";
    }

    @RequestMapping(value = "auth/reset", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    public String reset(User user, BindingResult result, @RequestParam(value = "token", required = false) String token, @RequestParam(value = "password1", required = false) String password1, @RequestParam(value = "password2", required = false) String password2, Model model, HttpServletRequest request, HttpServletResponse response) {
        String savedSessionId = (String) request.getSession().getAttribute("sessionId");
        String sessionId = request.getSession().getId();
        if (!sessionId.equals(savedSessionId)) {
            result.reject("", "Invalid token.");
            return "auth/reset";
        }

        String savedEmail = (String) request.getSession().getAttribute("email");
        user = User.findByUsername(savedEmail);
        if (user == null) {
            result.reject("", "Invalid Email address");
        }

        String savedToken = (String) request.getSession().getAttribute("token");
        if (!token.equals(savedToken)) {
            result.reject("", "Invalid token");
        }

        if (password1 == null || password1.isEmpty() || password2 == null || password2.isEmpty()) {
            result.reject("", "You must provide a password");
        }

        if (!password1.equals(password2)) {
            result.reject("", "Make sure the two passwords are the same");
        }

        if (result.hasErrors()) {
            return "auth/reset";
        }

        // at last
        ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
        String salt = SecurityHelper.generateSalt(encoder);
        user.setSalt(salt);
        user.setPassword(encoder.encodePassword(password2, salt));
        user.merge();

        request.getSession().removeAttribute("sessionId");
        request.getSession().removeAttribute("email");
        request.getSession().removeAttribute("token");

        return "redirect:/auth/signin";
    }
}
