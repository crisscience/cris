/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.web.handler;

import edu.purdue.cybercenter.dm.domain.Tool;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class PermissionHandler extends HandlerInterceptorAdapter {

    private static final String ResourceNotFound = "/resourceNotFound";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        return super.preHandle(request, response, handler);

        /***********************
         * access control
         ***********************/
        /*
        if (WebHelper.isResourceRequest(request)) {
            return true;
        }

        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        User user = User.findUser(userId);
        if (user != null && !user.isAdmin()) {
            Session session = Helper.getHbmSession();
            session.enableFilter("myToolsFilter").setParameter("userId", user.getId());
        }

        Integer userId = (Integer) request.getSession().getAttribute("userId");
        Integer groupId = (Integer) request.getSession().getAttribute("groupId");
        Boolean owner = (Boolean) request.getSession().getAttribute("owner");
        Boolean provider = (Boolean) request.getSession().getAttribute("provider");

        User user = User.findUser(userId);
        Group group = Group.findGroup(groupId);

        request.setAttribute("user", user);
        request.setAttribute("group", group);
        request.setAttribute("owner", owner);
        request.setAttribute("provider", provider);

        if (user != null) {
        List<Group> memberGroups = Group.findGroupsByUserId(userId);
        List<Group> ownerGroups = Group.findGroupsByOwnerId(user).getResultList();
        request.setAttribute("memberGroups", memberGroups);
        request.setAttribute("ownerGroups", ownerGroups);
        }
         */

        /***********************
         * TODO: access control
         * 1. Based on roles
         * 2. Based on permissions
         ***********************/
//        EntityManager em = user.entityManager();
//        Session session = (Session) em.getDelegate();
//        session.enableFilter("userPermission").setParameter("user", user);

        /*
        System.out.println("========================================");
        System.out.println("User: " + user);
        System.out.println("Servlet Path: " + request.getServletPath());
        System.out.println("Handler: " + handler.getClass().getName());

        String servletPath = request.getServletPath();
        if (servletPath != null && servletPath.equals("/users")) {
        request.getRequestDispatcher(ResourceNotFound).forward(request, response);
        return false;
        }

        return true;
         *
         */
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            User user = User.findUser(userId);
            Session session = DomainObjectHelper.getHbmSession();
            if (user != null && session.isOpen()) {
                Map<String, Object> model = modelAndView.getModel();
                if (user.isAdmin()) {
                    // What is the first thing an admin like to see
                    // 1. A list of administrative tasks layed out nicely
                } else {
                    // TODO: find all permitted tools for the user
                    // user
                    session.enableFilter("myToolsFilter").setParameter("userId", user.getId());
                }

                try {
                    List<Tool> tools = Tool.findAllTools();
                    model.put("tools", tools);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }

                model.put("user", user);
            }
        }

        return;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }
}
