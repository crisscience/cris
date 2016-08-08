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
import edu.purdue.cybercenter.dm.security.UserDetailsAdapter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.AclPermissionEvaluator;
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
    private AclPermissionEvaluator aclPermissionEvaluator;

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
            if (user.isAdmin() || aclPermissionEvaluator.hasPermission(authentication, project, BasePermission.READ)) {
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
            if (user.isAdmin() || aclPermissionEvaluator.hasPermission(authentication, experiment, BasePermission.READ)) {
                experimentIds.add(experiment.getId());
            }
        }
        
        return experimentIds;
    }

}
