package edu.purdue.cybercenter.dm.web.filter;

import edu.purdue.cybercenter.dm.domain.Constant;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import java.io.IOException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handling workflow as a session
 */
@Configurable
public class WorkflowAsSessionFilter implements Filter {

    static final private Logger logger = LoggerFactory.getLogger(WorkflowAsSessionFilter.class.getName());

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Default constructor.
     */
    public WorkflowAsSessionFilter() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param fConfig
     * @throws javax.servlet.ServletException
     * @see Filter#init(FilterConfig)
     */
    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        // TODO Auto-generated method stub
    }

    /**
     * @see Filter#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    /**
     * @param request
     * @param response
     * @param chain
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (WebHelper.isResourceRequest((HttpServletRequest) request)) {
            // pass the request along the filter chain
            chain.doFilter(request, response);
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpSession httpSession = httpRequest.getSession();

            String contextPath = httpRequest.getContextPath();
            String requestURI = httpRequest.getRequestURI();
            String pathInfo = requestURI.substring(contextPath.length());
            String method = httpRequest.getMethod();
            if (pathInfo != null && pathInfo.startsWith("/jobs/run")) {
                // Step 2.1: identify
                boolean isAuthenticated = httpSession.getAttribute("userId") != null;
                if (!isAuthenticated) {
                    boolean isValid = false;
                    String message = null;

                    String username = Constant.PublicUsername;
                    String password = "d41d62b0-3cbc-11e2-a25f-0800200c9a66";

                    User publicUser = User.findByUsername(username);
                    if (publicUser == null) {
                        message = "this workspace unable to run workflow anonymously";
                    } else {
                        httpSession.setAttribute("workflowAsSession", Boolean.TRUE);

                        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
                        if (authentication.isAuthenticated()) {
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            httpSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                            httpSession.setAttribute("userId", publicUser.getId());
                            httpSession.setAttribute("firstName", publicUser.getFirstName());
                        }

                        isValid = true;
                    }

                    if (!isValid) {
                        logger.warn(message);
                        String path = contextPath + "/resourceNotFound";
                        RequestDispatcher dispatcher = httpSession.getServletContext().getRequestDispatcher(path);
                        dispatcher.forward(request, response);
                        return;
                    }
                }
            } else {
                Boolean workflowAsSession = (Boolean) httpSession.getAttribute("workflowAsSession");
                if (workflowAsSession != null && workflowAsSession == true) {
                    // Step 2.2: enforce
                    boolean isValid = false;
                    String message = null;

                    if (pathInfo != null && pathInfo.startsWith("/jobs/task")) {
                        // make sure the task belongs to a job of the current session
                        String parts[] = pathInfo.split("/");
                        if (parts.length == 3 && method.equals("POST")) {
                            isValid = true;
                        } else if (parts.length == 4 && parts[3].matches("[0-9]+")) {
                            Integer jobId = Integer.parseInt(parts[3]);
                            List<Integer> jobIds = (List<Integer>) httpSession.getAttribute("jobIds");
                            if (jobIds.contains(jobId)) {
                                isValid = true;
                            } else {
                                message = "wrong job: " + jobId;
                            }
                        } else {
                            message = "invalid URL: " + pathInfo;
                        }
                    } else if (pathInfo != null && (pathInfo.startsWith("/rest/objectus") || pathInfo.startsWith("/download/EmbeddedFile") || pathInfo.startsWith("/auth/signout"))) {
                        //TODO: more security check if needed
                        isValid = true;
                    } else {
                        message = "invalid URL: " + pathInfo;
                    }

                    if (!isValid) {
                        logger.warn(message);
                        String path = contextPath + "/resourceNotFound";
                        RequestDispatcher dispatcher = httpSession.getServletContext().getRequestDispatcher(path);
                        dispatcher.forward(request, response);
                        return;
                    }
                }
            }

            // pass the request along the filter chain
            chain.doFilter(httpRequest, httpResponse);
        }
    }
}
