package edu.purdue.cybercenter.dm.web.filter;

import edu.purdue.cybercenter.dm.domain.Configuration;
import edu.purdue.cybercenter.dm.domain.Session;
import edu.purdue.cybercenter.dm.domain.Tenant;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import java.io.IOException;
import java.util.List;
import javax.persistence.Query;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;

/**
 * Servlet Filter implementation class SessionFilter
 */
public class SessionFilter implements Filter {

    public SessionFilter() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        // TODO Auto-generated method stub
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (WebHelper.isResourceRequest((HttpServletRequest) request)) {
            // pass the request along the filter chain
            chain.doFilter(request, response);
        } else {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpSession httpSession = httpRequest.getSession();

            String queryString = (httpRequest.getQueryString() == null || httpRequest.getQueryString().isEmpty()) ? "" : "?" + httpRequest.getQueryString();
            System.out.println("-----------------------New Request----------------------------------");
            System.out.println("==== Request: " + httpRequest.getMethod() + " " + httpRequest.getRequestURI() + queryString);

            // Fill in session info
            Session session = Session.findBySessionId(httpSession.getId());
            if (session.getHost() == null) {
                session.setHost(httpRequest.getRemoteAddr());
                session.setUserAgent(httpRequest.getHeader("User-Agent"));
                session.setRequestUrl(httpRequest.getRequestURL().toString());
                session.setReferer(httpRequest.getHeader("Referer"));
                session.merge();
            }

            // check for debug flag
            String debug = httpRequest.getParameter("debug");
            if (debug != null && debug.equals("true")) {
                httpSession.setAttribute("debug", true);
            } else if (debug != null && debug.equals("false")) {
                httpSession.setAttribute("debug", false);
            }

            // Associate a tenant with the session
            Integer tenantId = (Integer) httpSession.getAttribute("tenantId");
            if (tenantId == null) {
                // no workspace is identified
                String ctxPath = httpRequest.getContextPath();
                String uri = httpRequest.getRequestURI().substring(ctxPath.length());
                String[] parts = uri.split("/");
                String urlIdentifier = httpRequest.getParameter("tenantUrlIdentifier");
                boolean tenantInQuery = StringUtils.isNotEmpty(urlIdentifier);
                if (!tenantInQuery && (parts.length == 2 || (parts.length == 3 && parts[2].isEmpty()))) {
                    urlIdentifier = parts[1];
                    tenantInQuery = false;
                }

                if (urlIdentifier != null && !urlIdentifier.isEmpty()) {
                    Query query = DomainObjectHelper.createNamedQuery("Tenant.findByUrlIdentifier").setParameter("urlIdentifier", urlIdentifier);
                    try {
                        Tenant tenant = (Tenant) query.getSingleResult();
                        tenantId = tenant.getId();
                        httpSession.setAttribute("tenantId", tenantId);
                    } catch (Exception ex) {
                        // invalid workspace identifier
                        // handled by RequestFilter
                    }
                }

                // Application configuration
                // TODO: should they just be in the database?
                List<Configuration> configurations;
                if (tenantId != null) {
                    // Set tenant filter
                    org.hibernate.Session hSession = DomainObjectHelper.getHbmSession();
                    org.hibernate.Filter tenantFilter = hSession.enableFilter("tenantFilter");
                    tenantFilter.setParameter("tenantId", tenantId);

                    configurations = Configuration.findAllConfigurations();
                } else {
                    Query query = DomainObjectHelper.createNamedQuery("Configuration.findGlobals");
                    configurations = query.getResultList();
                }
                for (Configuration config : configurations) {
                    httpSession.setAttribute(config.getName(), config.getValueText());
                }

                // redirect to the home page of the workspace
                if (tenantId != null && !tenantInQuery) {
                    httpResponse.sendRedirect(httpRequest.getContextPath() + "/");
                    return;
                }
            }

            if (tenantId == null) {
                // no workspace identified: redirect to home page
                String uri = httpRequest.getRequestURI();
                String contextPath = httpRequest.getContextPath();
                String pathInfo = uri.substring(contextPath.length());
                if (StringUtils.isNotEmpty(pathInfo) && !pathInfo.equals("/") && !pathInfo.startsWith("/tenants")) {
                    // should be redirected to GET /
                    httpResponse.sendRedirect(StringUtils.isEmpty(contextPath) ? "/" : contextPath);
                    return;
                }
            } else {
                Tenant tenant = Tenant.findTenant(tenantId);
                edu.purdue.cybercenter.dm.threadlocal.TenantId.set(tenantId);
                httpRequest.setAttribute("tenant", tenant);

                // Set tenant filter
                org.hibernate.Session hSession = DomainObjectHelper.getHbmSession();
                org.hibernate.Filter tenantFilter = hSession.enableFilter("tenantFilter");
                tenantFilter.setParameter("tenantId", tenantId);

                // check for headless session
                String requestHeadless = httpRequest.getParameter("headless");
                if (requestHeadless != null) {
                    if (requestHeadless.equals("true")) {
                        httpRequest.getSession().setAttribute("headless", Boolean.TRUE);
                    } else {
                        httpRequest.getSession().setAttribute("headless", Boolean.FALSE);
                    }
                }
            }

            // pass the request along the filter chain
            chain.doFilter(request, response);
        }
    }

}
