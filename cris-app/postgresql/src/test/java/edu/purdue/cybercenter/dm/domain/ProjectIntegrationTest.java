package edu.purdue.cybercenter.dm.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@Transactional
@Configurable
public class ProjectIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private ProjectDataOnDemand dod;

	@Test
    public void testCountProjects() {
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to initialize correctly", dod.getRandomProject());
        long count = edu.purdue.cybercenter.dm.domain.Project.countProjects();
        org.junit.Assert.assertTrue("Counter for 'Project' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindProject() {
        edu.purdue.cybercenter.dm.domain.Project obj = dod.getRandomProject();
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Project.findProject(id);
        org.junit.Assert.assertNotNull("Find method for 'Project' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Project' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllProjects() {
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to initialize correctly", dod.getRandomProject());
        long count = edu.purdue.cybercenter.dm.domain.Project.countProjects();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Project', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Project> result = edu.purdue.cybercenter.dm.domain.Project.findAllProjects();
        org.junit.Assert.assertNotNull("Find all method for 'Project' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Project' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindProjectEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to initialize correctly", dod.getRandomProject());
        long count = edu.purdue.cybercenter.dm.domain.Project.countProjects();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Project> result = edu.purdue.cybercenter.dm.domain.Project.findProjectEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Project' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Project' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to initialize correctly", dod.getRandomProject());
        edu.purdue.cybercenter.dm.domain.Project obj = dod.getNewTransientProject(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Project' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Project' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Project obj = dod.getRandomProject();
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Project' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Project.findProject(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Project' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Project.findProject(id));
    }
}
