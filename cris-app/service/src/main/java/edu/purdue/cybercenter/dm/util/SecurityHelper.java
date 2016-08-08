/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.util;

import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.security.UserDetailsAdapter;
import java.security.SecureRandom;
import java.util.Random;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author xu222
 */
public class SecurityHelper {

    public static String generateSalt(ShaPasswordEncoder encoder) {
        Random r = new SecureRandom();
        byte[] rb = new byte[128];
        r.nextBytes(rb);
        String salt = encoder.encodePassword("", rb.toString());

        return salt;
    }

    public static void setAuthentication(Integer userId) {
        User user = User.findUser(userId);
        UserDetails userDetails = new UserDetailsAdapter(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
