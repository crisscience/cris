/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.repository;

import com.mysema.query.types.expr.BooleanExpression;
import edu.purdue.cybercenter.dm.domain.Project;
import edu.purdue.cybercenter.dm.domain.QProject;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author xu222
 */
@Configurable
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
public class CrisRepositoryTest {

    @Autowired
    private CrisRepository<Project> projectRepository;

    @Test
    public void testCrud() {
        assertTrue(projectRepository != null);

        long count = projectRepository.count();
        Assert.assertEquals("number of existing projects", 5, count);

        Project project = new Project();
        project.setName("test project");
        project.setDescription("this is a test project");

        Project projectCreate = projectRepository.save(project);
        long newCount = projectRepository.count();
        Assert.assertEquals("number of existing projects", count + 1, newCount);
        Assert.assertNotNull("before project id", project.getId());
        Assert.assertNotNull("after project id", projectCreate.getId());

        Project projectRead = projectRepository.findOne(projectCreate.getId());
        Assert.assertEquals("project name", projectCreate.getName(), projectRead.getName());
        Assert.assertEquals("project name", projectCreate.getDescription(), projectRead.getDescription());

        projectRead.setName("new project name");
        Project projectUpdate = projectRepository.save(projectRead);
        Assert.assertEquals("project name", "new project name", projectUpdate.getName());

        Assert.assertTrue("project exist", projectRepository.exists(project.getId()));
        projectRepository.delete(project);
        Assert.assertFalse("project does not exist", projectRepository.exists(project.getId()));
    }

    @Test
    public void testCrudList() {
        long count = projectRepository.count();

        Project project1 = new Project();
        project1.setName("test project 1");
        project1.setDescription("this is a test project 1");
        Project project2 = new Project();
        project2.setName("test project 2");
        project2.setDescription("this is a test project 2");
        Project project3 = new Project();
        project3.setName("test project 3");
        project3.setDescription("this is a test project 3");

        List<Project> projects = new ArrayList<>();
        projects.add(project1);
        projects.add(project2);
        projects.add(project3);

        List<Project> projectsCreate = projectRepository.save(projects);
        Assert.assertEquals("number of projects created", 3, projectsCreate.size());
        long newCount = projectRepository.count();
        Assert.assertEquals("number of existing projects", count + 3, newCount);

        List<Integer> projectIds = new ArrayList<>();
        projectIds.add(project1.getId());
        projectIds.add(project2.getId());
        projectIds.add(project3.getId());
        List<Project> projectsRead = projectRepository.findAll(projectIds);
        Assert.assertEquals("number of projects read", 3, projectsRead.size());

        project1.setName("new test project 1");
        project2.setName("new test project 2");
        project3.setName("new test project 3");
        List<Project> projectsUpdate = projectRepository.save(projectsRead);
        Assert.assertEquals("number of projects updated", 3, projectsUpdate.size());
        Assert.assertEquals("project 1 name", "new test project 1", projectsUpdate.get(0).getName());

        projectRepository.delete(projectsUpdate);
        newCount = projectRepository.count();
        Assert.assertEquals("number of existing projects", count, newCount);
    }

    @Test
    public void testQuery() {
        edu.purdue.cybercenter.dm.threadlocal.TenantId.set(1);
        edu.purdue.cybercenter.dm.threadlocal.UserId.set(1);

        long count = projectRepository.count();

        Project project1 = new Project();
        project1.setName("test project 1");
        project1.setDescription("this is a test project 1");
        Project project2 = new Project();
        project2.setName("test project 2");
        project2.setDescription("this is a test project 2");
        Project project3 = new Project();
        project3.setName("test project 3");
        project3.setDescription("this is a test project 3");
        Project project4 = new Project();
        project4.setName("test project 4");
        project4.setDescription("this is a test project 4");
        Project project5 = new Project();
        project5.setName("test project 5");
        project5.setDescription("this is a test project 5");

        List<Project> projects = new ArrayList<>();
        projects.add(project1);
        projects.add(project2);
        projects.add(project3);
        projects.add(project4);
        projects.add(project5);

        List<Project> projectsCreate = projectRepository.save(projects);
        Assert.assertEquals("number of projects created", 5, projectsCreate.size());
        long newCount = projectRepository.count();
        Assert.assertEquals("number of existing projects", count + 5, newCount);

        // test query, sort and pagination
        BooleanExpression expression = QProject.project.name.startsWithIgnoreCase("test project ");

        Order order = new Order("name");
        Sort sort = new Sort(order);

        // first page: 2 projects
        Pageable pageable1 = new PageRequest(0, 2, sort);
        Page<Project> projectsFind1 = projectRepository.findAll(expression, pageable1);
        Assert.assertEquals("projects in page 1", 2, projectsFind1.getContent().size());
        Assert.assertEquals("projects page number", 0, projectsFind1.getNumber());
        Assert.assertEquals("projects total", 5, projectsFind1.getTotalElements());
        Assert.assertTrue("first page", projectsFind1.isFirst());
        Assert.assertFalse("last page", projectsFind1.isLast());
        Assert.assertFalse("has previous page", projectsFind1.hasPrevious());
        Assert.assertTrue("has next page", projectsFind1.hasNext());

        // middle page: 2 projects
        Pageable pageable2 = projectsFind1.nextPageable();
        Page<Project> projectsFind2 = projectRepository.findAll(expression, pageable2);
        Assert.assertEquals("projects in page 2", 2, projectsFind2.getContent().size());
        Assert.assertEquals("projects page number", 1, projectsFind2.getNumber());
        Assert.assertFalse("first page", projectsFind2.isFirst());
        Assert.assertFalse("last page", projectsFind2.isLast());
        Assert.assertTrue("has previous page", projectsFind2.hasPrevious());
        Assert.assertTrue("has next page", projectsFind2.hasNext());

        // last page: 1 project
        Pageable pageable3 = projectsFind2.nextPageable();
        Page<Project> projectsFind3 = projectRepository.findAll(expression, pageable3);
        Assert.assertEquals("projects in page 3", 1, projectsFind3.getContent().size());
        Assert.assertEquals("projects page number", 2, projectsFind3.getNumber());
        Assert.assertFalse("first page", projectsFind3.isFirst());
        Assert.assertTrue("last page", projectsFind3.isLast());
        Assert.assertTrue("has previous page", projectsFind3.hasPrevious());
        Assert.assertFalse("has next page", projectsFind3.hasNext());

        // one after the last page: a single page?
        Pageable pageable4 = projectsFind3.nextPageable();
        Page<Project> projectsFind4 = projectRepository.findAll(expression, pageable4);
        Assert.assertEquals("projects in page 4", 5, projectsFind4.getContent().size());
        Assert.assertEquals("projects page number", 0, projectsFind4.getNumber());
        Assert.assertTrue("first page", projectsFind4.isFirst());
        Assert.assertTrue("last page", projectsFind4.isLast());
        Assert.assertFalse("has previous page", projectsFind4.hasPrevious());
        Assert.assertFalse("has next page", projectsFind4.hasNext());
    }
}
