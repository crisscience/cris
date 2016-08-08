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
public class StorageIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private StorageDataOnDemand dod;

	@Test
    public void testCountStorages() {
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to initialize correctly", dod.getRandomStorage());
        long count = edu.purdue.cybercenter.dm.domain.Storage.countStorages();
        org.junit.Assert.assertTrue("Counter for 'Storage' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindStorage() {
        edu.purdue.cybercenter.dm.domain.Storage obj = dod.getRandomStorage();
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Storage.findStorage(id);
        org.junit.Assert.assertNotNull("Find method for 'Storage' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Storage' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllStorages() {
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to initialize correctly", dod.getRandomStorage());
        long count = edu.purdue.cybercenter.dm.domain.Storage.countStorages();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Storage', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Storage> result = edu.purdue.cybercenter.dm.domain.Storage.findAllStorages();
        org.junit.Assert.assertNotNull("Find all method for 'Storage' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Storage' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindStorageEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to initialize correctly", dod.getRandomStorage());
        long count = edu.purdue.cybercenter.dm.domain.Storage.countStorages();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Storage> result = edu.purdue.cybercenter.dm.domain.Storage.findStorageEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Storage' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Storage' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to initialize correctly", dod.getRandomStorage());
        edu.purdue.cybercenter.dm.domain.Storage obj = dod.getNewTransientStorage(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Storage' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Storage' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Storage obj = dod.getRandomStorage();
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Storage' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Storage.findStorage(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Storage' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Storage.findStorage(id));
    }
}
