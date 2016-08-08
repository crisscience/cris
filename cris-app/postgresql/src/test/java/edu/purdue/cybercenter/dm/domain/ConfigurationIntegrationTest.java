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
public class ConfigurationIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private ConfigurationDataOnDemand dod;

	@Test
    public void testCountConfigurations() {
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to initialize correctly", dod.getRandomConfiguration());
        long count = edu.purdue.cybercenter.dm.domain.Configuration.countConfigurations();
        org.junit.Assert.assertTrue("Counter for 'Configuration' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindConfiguration() {
        edu.purdue.cybercenter.dm.domain.Configuration obj = dod.getRandomConfiguration();
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Configuration.findConfiguration(id);
        org.junit.Assert.assertNotNull("Find method for 'Configuration' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Configuration' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllConfigurations() {
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to initialize correctly", dod.getRandomConfiguration());
        long count = edu.purdue.cybercenter.dm.domain.Configuration.countConfigurations();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Configuration', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Configuration> result = edu.purdue.cybercenter.dm.domain.Configuration.findAllConfigurations();
        org.junit.Assert.assertNotNull("Find all method for 'Configuration' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Configuration' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindConfigurationEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to initialize correctly", dod.getRandomConfiguration());
        long count = edu.purdue.cybercenter.dm.domain.Configuration.countConfigurations();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Configuration> result = edu.purdue.cybercenter.dm.domain.Configuration.findConfigurationEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Configuration' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Configuration' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to initialize correctly", dod.getRandomConfiguration());
        edu.purdue.cybercenter.dm.domain.Configuration obj = dod.getNewTransientConfiguration(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Configuration' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Configuration' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Configuration obj = dod.getRandomConfiguration();
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Configuration' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Configuration.findConfiguration(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Configuration' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Configuration.findConfiguration(id));
    }
}
