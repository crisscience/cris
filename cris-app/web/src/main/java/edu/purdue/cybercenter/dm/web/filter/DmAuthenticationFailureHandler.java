/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web.filter;

import edu.purdue.cybercenter.dm.threadlocal.TenantId;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 *
 * @author xu222
 */
public class DmAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "LoginFailure");
        message.put("tenantId", TenantId.get());
        message.put("username", request.getParameter("username"));
        message.put("reason", exception.getMessage());
        message.put("date", (new Date()).toString());
        message.put("remote_address", request.getRemoteAddr());
        jmsTemplate.convertAndSend("queueLoginFailure", message);

        super.onAuthenticationFailure(request, response, exception);
    }
}
