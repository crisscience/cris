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
public class StorageFileIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private StorageFileDataOnDemand dod;

	@Test
    public void testCountStorageFiles() {
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to initialize correctly", dod.getRandomStorageFile());
        long count = edu.purdue.cybercenter.dm.domain.StorageFile.countStorageFiles();
        org.junit.Assert.assertTrue("Counter for 'StorageFile' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindStorageFile() {
        edu.purdue.cybercenter.dm.domain.StorageFile obj = dod.getRandomStorageFile();
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.StorageFile.findStorageFile(id);
        org.junit.Assert.assertNotNull("Find method for 'StorageFile' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'StorageFile' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllStorageFiles() {
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to initialize correctly", dod.getRandomStorageFile());
        long count = edu.purdue.cybercenter.dm.domain.StorageFile.countStorageFiles();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'StorageFile', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.StorageFile> result = edu.purdue.cybercenter.dm.domain.StorageFile.findAllStorageFiles();
        org.junit.Assert.assertNotNull("Find all method for 'StorageFile' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'StorageFile' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindStorageFileEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to initialize correctly", dod.getRandomStorageFile());
        long count = edu.purdue.cybercenter.dm.domain.StorageFile.countStorageFiles();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.StorageFile> result = edu.purdue.cybercenter.dm.domain.StorageFile.findStorageFileEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'StorageFile' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'StorageFile' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to initialize correctly", dod.getRandomStorageFile());
        edu.purdue.cybercenter.dm.domain.StorageFile obj = dod.getNewTransientStorageFile(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'StorageFile' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'StorageFile' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.StorageFile obj = dod.getRandomStorageFile();
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'StorageFile' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.StorageFile.findStorageFile(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'StorageFile' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.StorageFile.findStorageFile(id));
    }
}
