/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.Experiment;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.User;
import edu.purdue.cybercenter.dm.repository.ExperimentRepository;
import edu.purdue.cybercenter.dm.repository.ProjectRepository;
import edu.purdue.cybercenter.dm.security.CustomPermission;
import edu.purdue.cybercenter.dm.security.UserDetailsAdapter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class SecurityService {

    @Autowired
    private PermissionEvaluator permissionEvaluator;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ExperimentRepository experimentRepository;

    public List<Integer> getPermittedProjectIds() {
        List<Integer> projectIds = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((UserDetailsAdapter) authentication.getPrincipal()).getUser();
        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            boolean isPublic = project.getIsPublic() == null ? false : project.getIsPublic();
            if (user.isAdmin() || isPublic || permissionEvaluator.hasPermission(authentication, project.getId(), project.getClass().getName(), BasePermission.READ)) {
                projectIds.add(project.getId());
            }
        }

        return projectIds;
    }

    public List<Integer> getPermittedExperimentIds() {
        List<Integer> experimentIds = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((UserDetailsAdapter) authentication.getPrincipal()).getUser();
        List<Experiment> experiments = experimentRepository.findAll();
        for (Experiment experiment : experiments) {
            boolean isPublic = experiment.getIsPublic() == null ? false : experiment.getIsPublic();
            if (user.isAdmin() || isPublic || permissionEvaluator.hasPermission(authentication, experiment.getId(), experiment.getClass().getName(), BasePermission.READ)) {
                experimentIds.add(experiment.getId());
            }
        }

        return experimentIds;
    }

    public List<Integer> getOwnerExperimentIds() {
        List<Integer> experimentIds = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Experiment> experiments = experimentRepository.findAll();
        for (Experiment experiment : experiments) {
            if (permissionEvaluator.hasPermission(authentication, experiment.getId(), experiment.getClass().getName(), CustomPermission.OWNER)) {
                experimentIds.add(experiment.getId());
            }
        }

        return experimentIds;
    }

    public List<Integer> getOwnerProjectIds() {
        List<Integer> projectIds = new ArrayList<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            if (permissionEvaluator.hasPermission(authentication, project.getId(), project.getClass().getName(), CustomPermission.OWNER)) {
                projectIds.add(project.getId());
            }
        }

        return projectIds;
    }

}
