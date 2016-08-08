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
public class ComputationalNodeIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private ComputationalNodeDataOnDemand dod;

	@Test
    public void testCountComputationalNodes() {
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to initialize correctly", dod.getRandomComputationalNode());
        long count = edu.purdue.cybercenter.dm.domain.ComputationalNode.countComputationalNodes();
        org.junit.Assert.assertTrue("Counter for 'ComputationalNode' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindComputationalNode() {
        edu.purdue.cybercenter.dm.domain.ComputationalNode obj = dod.getRandomComputationalNode();
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.ComputationalNode.findComputationalNode(id);
        org.junit.Assert.assertNotNull("Find method for 'ComputationalNode' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'ComputationalNode' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllComputationalNodes() {
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to initialize correctly", dod.getRandomComputationalNode());
        long count = edu.purdue.cybercenter.dm.domain.ComputationalNode.countComputationalNodes();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'ComputationalNode', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.ComputationalNode> result = edu.purdue.cybercenter.dm.domain.ComputationalNode.findAllComputationalNodes();
        org.junit.Assert.assertNotNull("Find all method for 'ComputationalNode' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'ComputationalNode' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindComputationalNodeEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to initialize correctly", dod.getRandomComputationalNode());
        long count = edu.purdue.cybercenter.dm.domain.ComputationalNode.countComputationalNodes();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.ComputationalNode> result = edu.purdue.cybercenter.dm.domain.ComputationalNode.findComputationalNodeEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'ComputationalNode' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'ComputationalNode' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to initialize correctly", dod.getRandomComputationalNode());
        edu.purdue.cybercenter.dm.domain.ComputationalNode obj = dod.getNewTransientComputationalNode(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'ComputationalNode' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'ComputationalNode' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.ComputationalNode obj = dod.getRandomComputationalNode();
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'ComputationalNode' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.ComputationalNode.findComputationalNode(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'ComputationalNode' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.ComputationalNode.findComputationalNode(id));
    }
}
