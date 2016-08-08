package edu.purdue.cybercenter.dm.web.filter;

import edu.purdue.cybercenter.dm.security.UserDetailsAdapter;
import edu.purdue.cybercenter.dm.domain.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 *
 * @author xu222
 */
public class RestLoginFilter extends AbstractAuthenticationProcessingFilter {

    protected RestLoginFilter() {
        super("/j_spring_security_check");
    }

    protected RestLoginFilter(String url) {
        super(url);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String username = request.getHeader("username");
        String password = request.getHeader("password");

        Authentication authentication = null;
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            User user = User.findByUsername(username);
            UserDetails uda = new UserDetailsAdapter(user);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, uda.getAuthorities());
            authentication = this.getAuthenticationManager().authenticate(token);

            if (authentication != null) {
                RequestCache requestCache = new HttpSessionRequestCache();
                requestCache.saveRequest(request, response);
                ((SavedRequestAwareAuthenticationSuccessHandler) this.getSuccessHandler()).setRequestCache(requestCache);
            }
        }

        return authentication;
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getHeader("username");
        String password = request.getHeader("password");

        if (username == null && password == null) {
            return false;
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                Object principal = authentication.getPrincipal();
                String loggedInUsername;
                if (principal instanceof UserDetails) {
                    loggedInUsername = ((UserDetails) principal).getUsername();
                } else {
                    loggedInUsername = principal.toString();
                }
                if (loggedInUsername != null && loggedInUsername.equals(username)) {
                    return false;
                }
            }
        }

        return true;
    }
}
