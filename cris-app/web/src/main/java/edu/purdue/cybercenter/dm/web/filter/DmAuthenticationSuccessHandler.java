/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web.filter;

import edu.purdue.cybercenter.dm.domain.Group;
import edu.purdue.cybercenter.dm.domain.Session;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.domain.UserSession;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 *
 * @author xu222
 */
public class DmAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        User user = User.findByUsername(authentication.getName());
        edu.purdue.cybercenter.dm.threadlocal.UserId.set(user.getId());

        HttpSession httpSession = request.getSession();

        Session session = Session.findBySessionId(httpSession.getId());
        if (session == null) {
            session = new Session();
            session.setJsessionid(httpSession.getId());
            session.persist();
        }

        // update session information
        session.setHost(request.getRemoteAddr());
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setRequestUrl(request.getRequestURL().toString());
        session.setReferer(request.getHeader("Referer"));

        UserSession us = new UserSession();
        us.setUserId(user);
        us.setSessionId(session);
        us.persist();

        // Write user id & first name into session for easy access
        httpSession.setAttribute("userId", user.getId());
        httpSession.setAttribute("firstName", user.getFirstName());

        List<Group> groups = user.getMemberGroups();
        if (groups != null && groups.size() == 1) {
            // set the current group
            httpSession.setAttribute("groupId", groups.get(0).getId());
        }

        super.onAuthenticationSuccess(request, response, authentication);

    }
}
