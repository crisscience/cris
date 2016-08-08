package edu.purdue.cybercenter.dm.web;

import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.threadlocal.UserId;
import edu.purdue.cybercenter.dm.util.SecurityHelper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/profile")
@Controller
public class ProfileController {

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        Integer userId = UserId.get();
        User user = User.findUser(userId);
        model.addAttribute("user", user);

        return "profile/index";
    }

    @RequestMapping(value = "/password", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    public String changePassword(@RequestParam(value = "password0", required = false) String password0, @RequestParam(value = "password1", required = false) String password1, @RequestParam(value = "password2", required = false) String password2, @Valid User user, BindingResult result, Model model) {
        Boolean hasError = false;
        if (user == null) {
            result.reject("", "Unable to verify your identity. Logout and sign in again may help");
            hasError = true;
        }
        if (password0 != null && !password0.isEmpty()) {
            // Check if current password is valid
            ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
            String dbSalt = user.getSalt();
            String dbPassowrd = user.getPassword();
            String inPassowrd0 = encoder.encodePassword(password0, dbSalt);
            String inPassowrd1 = encoder.encodePassword(password1, dbSalt);

            if (!dbPassowrd.equals(inPassowrd0)) {
                result.reject("", "Current password is wrong");
                hasError = true;
            }

            // check if the two new passwords are the same
            if (password1 == null || password1.isEmpty() || password2 == null || password2.isEmpty()) {
                result.reject("", "New password cannot be empty");
                hasError = true;
            } else if (!password1.equals(password2)) {
                result.reject("", "The two passwords does not match");
                hasError = true;
            }

            // compare the current password with the new one
            if (dbPassowrd.equals(inPassowrd1)) {
                result.reject("", "The new password is the same as the current one");
                hasError = true;
            }

            if (hasError) {
                return "profile/index";
            }

            // save the new password
            String salt = SecurityHelper.generateSalt(encoder);
            String password = encoder.encodePassword(password1, salt);;
            user.setSalt(salt);
            user.setPassword(password);
            result.reject("", "Your password has been changed successfully");
        }

        User newUser = User.findByUsername(user.getUsername());
        if (newUser != null && !newUser.getId().equals(user.getId())) {
            result.reject("", "Username \"" + user.getUsername() + "\" has already been taken.");
            return "profile/index";
        }

        try {
            user.persist();
            result.reject("", "Your name has been changed successfully");
        } catch (Exception ex) {
            result.reject("", "Unable to save your changes");
        }

        return "profile/index";
    }

    @ModelAttribute("user")
    public User populateUser(HttpServletRequest request, HttpServletResponse response) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        User user = User.findUser(userId);
        return user;
    }
}
