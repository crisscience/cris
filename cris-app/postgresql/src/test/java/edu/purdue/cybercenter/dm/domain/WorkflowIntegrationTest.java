package edu.purdue.cybercenter.dm.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml")
@Transactional
public class WorkflowIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private WorkflowDataOnDemand dod;

	@Test
    public void testCountWorkflows() {
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to initialize correctly", dod.getRandomWorkflow());
        long count = edu.purdue.cybercenter.dm.domain.Workflow.countWorkflows();
        org.junit.Assert.assertTrue("Counter for 'Workflow' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindWorkflow() {
        edu.purdue.cybercenter.dm.domain.Workflow obj = dod.getRandomWorkflow();
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Workflow.findWorkflow(id);
        org.junit.Assert.assertNotNull("Find method for 'Workflow' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Workflow' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllWorkflows() {
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to initialize correctly", dod.getRandomWorkflow());
        long count = edu.purdue.cybercenter.dm.domain.Workflow.countWorkflows();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Workflow', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Workflow> result = edu.purdue.cybercenter.dm.domain.Workflow.findAllWorkflows();
        org.junit.Assert.assertNotNull("Find all method for 'Workflow' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Workflow' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindWorkflowEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to initialize correctly", dod.getRandomWorkflow());
        long count = edu.purdue.cybercenter.dm.domain.Workflow.countWorkflows();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Workflow> result = edu.purdue.cybercenter.dm.domain.Workflow.findWorkflowEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Workflow' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Workflow' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to initialize correctly", dod.getRandomWorkflow());
        edu.purdue.cybercenter.dm.domain.Workflow obj = dod.getNewTransientWorkflow(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Workflow' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Workflow' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Workflow obj = dod.getRandomWorkflow();
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Workflow' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Workflow.findWorkflow(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Workflow' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Workflow.findWorkflow(id));
    }
}
