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
public class ExperimentIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private ExperimentDataOnDemand dod;

	@Test
    public void testCountExperiments() {
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to initialize correctly", dod.getRandomExperiment());
        long count = edu.purdue.cybercenter.dm.domain.Experiment.countExperiments();
        org.junit.Assert.assertTrue("Counter for 'Experiment' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindExperiment() {
        edu.purdue.cybercenter.dm.domain.Experiment obj = dod.getRandomExperiment();
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Experiment.findExperiment(id);
        org.junit.Assert.assertNotNull("Find method for 'Experiment' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Experiment' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllExperiments() {
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to initialize correctly", dod.getRandomExperiment());
        long count = edu.purdue.cybercenter.dm.domain.Experiment.countExperiments();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Experiment', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Experiment> result = edu.purdue.cybercenter.dm.domain.Experiment.findAllExperiments();
        org.junit.Assert.assertNotNull("Find all method for 'Experiment' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Experiment' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindExperimentEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to initialize correctly", dod.getRandomExperiment());
        long count = edu.purdue.cybercenter.dm.domain.Experiment.countExperiments();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Experiment> result = edu.purdue.cybercenter.dm.domain.Experiment.findExperimentEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Experiment' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Experiment' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to initialize correctly", dod.getRandomExperiment());
        edu.purdue.cybercenter.dm.domain.Experiment obj = dod.getNewTransientExperiment(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Experiment' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Experiment' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Experiment obj = dod.getRandomExperiment();
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Experiment' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Experiment.findExperiment(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Experiment' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Experiment.findExperiment(id));
    }
}
