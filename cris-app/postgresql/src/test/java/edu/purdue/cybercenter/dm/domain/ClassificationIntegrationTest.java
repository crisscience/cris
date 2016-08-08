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
public class ClassificationIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

    @Autowired
    private ClassificationDataOnDemand dod;

    @Test
    public void testCountClassifications() {
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to initialize correctly", dod.getRandomClassification());
        long count = edu.purdue.cybercenter.dm.domain.Classification.countClassifications();
        org.junit.Assert.assertTrue("Counter for 'Classification' incorrectly reported there were no entries", count > 0);
    }

    @Test
    public void testFindClassification() {
        edu.purdue.cybercenter.dm.domain.Classification obj = dod.getRandomClassification();
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Classification.findClassification(id);
        org.junit.Assert.assertNotNull("Find method for 'Classification' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Classification' returned the incorrect identifier", id, obj.getId());
    }

    @Test
    public void testFindAllClassifications() {
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to initialize correctly", dod.getRandomClassification());
        long count = edu.purdue.cybercenter.dm.domain.Classification.countClassifications();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Classification', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Classification> result = edu.purdue.cybercenter.dm.domain.Classification.findAllClassifications();
        org.junit.Assert.assertNotNull("Find all method for 'Classification' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Classification' failed to return any data", result.size() > 0);
    }

    @Test
    public void testFindClassificationEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to initialize correctly", dod.getRandomClassification());
        long count = edu.purdue.cybercenter.dm.domain.Classification.countClassifications();
        if (count > 20) {
            count = 20;
        }
        java.util.List<edu.purdue.cybercenter.dm.domain.Classification> result = edu.purdue.cybercenter.dm.domain.Classification.findClassificationEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Classification' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Classification' returned an incorrect number of entries", count, result.size());
    }

    @Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to initialize correctly", dod.getRandomClassification());
        edu.purdue.cybercenter.dm.domain.Classification obj = dod.getNewTransientClassification(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Classification' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Classification' identifier to no longer be null", obj.getId());
    }

    @Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Classification obj = dod.getRandomClassification();
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Classification' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Classification.findClassification(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Classification' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Classification.findClassification(id));
    }
}
