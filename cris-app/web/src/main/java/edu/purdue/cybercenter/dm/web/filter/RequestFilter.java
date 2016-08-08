/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web.filter;

import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.web.util.WebHelper;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.TilesContainer;
import org.apache.tiles.servlet.context.ServletUtil;

public class RequestFilter implements Filter {

    private static final boolean debug = false;
    private static final String ViewRoot = "/WEB-INF/views/";
    private static final String Tile_Menu = "menu";
    private static final String Page_Menu = "menu";
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured.
    private FilterConfig filterConfig = null;

    public RequestFilter() {
    }

    /**
     * Init method for this filter
     * @param filterConfig
     */
    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("RequestFilter: Initializing filter");
            }
        }
    }

    /**
     * Destroy method for this filter
     */
    @Override
    public void destroy() {
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (debug) {
            log("RequestFilter:doFilter()");
        }

        doBeforeProcessing((HttpServletRequest) request, (HttpServletResponse) response);

        Throwable problem = null;

        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            // If an exception is thrown somewhere down the filter chain,
            // we still want to execute our after processing, and then
            // rethrow the problem after that.
            problem = t;
            //t.printStackTrace();
        }

        doAfterProcessing((HttpServletRequest) request, (HttpServletResponse) response);

        // If there was a problem, we want to rethrow it if it is
        // a known type, otherwise log it.
        if (problem != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            sendProcessingError(problem, response);
        }
    }

    /**
     * Return the filter configuration object for this filter.
     * @return
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("RequestFilter()");
        }
        StringBuilder sb = new StringBuilder("RequestFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());

    }

    private void doBeforeProcessing(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        if (WebHelper.isResourceRequest(request)) {
            return;
        }

        HttpSession httpSession = request.getSession();
        Integer userId = (Integer) httpSession.getAttribute("userId");
        User user = User.findUser(userId);
        if (user == null) {
            return;
        }

        edu.purdue.cybercenter.dm.threadlocal.UserId.set(userId);
        request.setAttribute("user", user);

        Boolean headless = (Boolean) httpSession.getAttribute("headless");
        if (headless == null || !headless) {
            // Setup menu
            String managerOrStaff = "_user";
            String providerOrConsumer = "";
            if (user.isAdmin()) {
                String view = request.getParameter("view");
                List<String> views = (List<String>) httpSession.getAttribute("views");
                if (view != null && views.contains(view.toLowerCase())) {
                    httpSession.setAttribute("currentView", view.toLowerCase());
                } else {
                    view = (String) httpSession.getAttribute("currentView");
                }
                if (view != null && view.equalsIgnoreCase("user")) {
                    managerOrStaff = "_user";
                } else {
                    managerOrStaff = "_admin";
                }
                providerOrConsumer = "";
            }
            TilesContainer container = ServletUtil.getContainer(httpSession.getServletContext());
            AttributeContext attributeContext = container.getAttributeContext(request, response);
            attributeContext.putAttribute(Tile_Menu, new Attribute(ViewRoot + Page_Menu + managerOrStaff + providerOrConsumer + ".jspx"));
        }

        org.hibernate.Session hSession = DomainObjectHelper.getHbmSession();
        // by default use only operational asset, i.e. status == 1
        String showAllStatus = request.getParameter("showAllStatus");
        if (showAllStatus == null || showAllStatus.equals("false")) {
            org.hibernate.Filter assetStatusFilter = hSession.enableFilter("assetStatusFilter");
            assetStatusFilter.setParameterList("statusIds", Arrays.asList(1));
        }

        // everyone can only see his/her tiles
        org.hibernate.Filter myTilesFilter = hSession.enableFilter("myTilesFilter");
        myTilesFilter.setParameter("userId", userId);
    }

    private void doAfterProcessing(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (debug) {
            log("RequestFilter:DoAfterProcessing");
        }

    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        try {
            if (stackTrace != null && !stackTrace.equals("")) {
                response.setContentType("text/html");
                try (PrintStream ps = new PrintStream(response.getOutputStream()); PrintWriter pw = new PrintWriter(ps)) {
                    pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                    // PENDING! Localize this for next official release
                    pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                    pw.print(stackTrace);
                    pw.print("</pre></body>\n</html>"); //NOI18N
                }
            } else {
                try (PrintStream ps = new PrintStream(response.getOutputStream())) {
                    t.printStackTrace(ps);
                }
            }
            response.getOutputStream().close();
        } catch (IOException ex) {
        }
    }

    private static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (IOException ex) {
        }
        return stackTrace;
    }

    private void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }

}
