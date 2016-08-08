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
public class ResourceIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private ResourceDataOnDemand dod;

	@Test
    public void testCountResources() {
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to initialize correctly", dod.getRandomResource());
        long count = edu.purdue.cybercenter.dm.domain.Resource.countResources();
        org.junit.Assert.assertTrue("Counter for 'Resource' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindResource() {
        edu.purdue.cybercenter.dm.domain.Resource obj = dod.getRandomResource();
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Resource.findResource(id);
        org.junit.Assert.assertNotNull("Find method for 'Resource' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Resource' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllResources() {
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to initialize correctly", dod.getRandomResource());
        long count = edu.purdue.cybercenter.dm.domain.Resource.countResources();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Resource', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Resource> result = edu.purdue.cybercenter.dm.domain.Resource.findAllResources();
        org.junit.Assert.assertNotNull("Find all method for 'Resource' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Resource' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindResourceEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to initialize correctly", dod.getRandomResource());
        long count = edu.purdue.cybercenter.dm.domain.Resource.countResources();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Resource> result = edu.purdue.cybercenter.dm.domain.Resource.findResourceEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Resource' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Resource' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to initialize correctly", dod.getRandomResource());
        edu.purdue.cybercenter.dm.domain.Resource obj = dod.getNewTransientResource(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Resource' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Resource' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Resource obj = dod.getRandomResource();
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Resource' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Resource.findResource(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Resource' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Resource.findResource(id));
    }
}
