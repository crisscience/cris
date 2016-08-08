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
public class ToolIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private ToolDataOnDemand dod;

	@Test
    public void testCountTools() {
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to initialize correctly", dod.getRandomTool());
        long count = edu.purdue.cybercenter.dm.domain.Tool.countTools();
        org.junit.Assert.assertTrue("Counter for 'Tool' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindTool() {
        edu.purdue.cybercenter.dm.domain.Tool obj = dod.getRandomTool();
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Tool.findTool(id);
        org.junit.Assert.assertNotNull("Find method for 'Tool' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Tool' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllTools() {
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to initialize correctly", dod.getRandomTool());
        long count = edu.purdue.cybercenter.dm.domain.Tool.countTools();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Tool', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Tool> result = edu.purdue.cybercenter.dm.domain.Tool.findAllTools();
        org.junit.Assert.assertNotNull("Find all method for 'Tool' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Tool' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindToolEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to initialize correctly", dod.getRandomTool());
        long count = edu.purdue.cybercenter.dm.domain.Tool.countTools();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Tool> result = edu.purdue.cybercenter.dm.domain.Tool.findToolEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Tool' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Tool' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to initialize correctly", dod.getRandomTool());
        edu.purdue.cybercenter.dm.domain.Tool obj = dod.getNewTransientTool(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Tool' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Tool' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Tool obj = dod.getRandomTool();
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Tool' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Tool.findTool(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Tool' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Tool.findTool(id));
    }
}
