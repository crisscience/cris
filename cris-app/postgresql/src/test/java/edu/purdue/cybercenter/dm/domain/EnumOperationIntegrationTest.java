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
public class EnumOperationIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

    @Test
    public void testPersist() {
    }

    @Test
    public void testRemove() {
    }

	@Autowired
    private EnumOperationDataOnDemand dod;

	@Test
    public void testCountEnumOperations() {
        org.junit.Assert.assertNotNull("Data on demand for 'EnumOperation' failed to initialize correctly", dod.getRandomEnumOperation());
        long count = edu.purdue.cybercenter.dm.domain.EnumOperation.countEnumOperations();
        org.junit.Assert.assertTrue("Counter for 'EnumOperation' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindEnumOperation() {
        edu.purdue.cybercenter.dm.domain.EnumOperation obj = dod.getRandomEnumOperation();
        org.junit.Assert.assertNotNull("Data on demand for 'EnumOperation' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'EnumOperation' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.EnumOperation.findEnumOperation(id);
        org.junit.Assert.assertNotNull("Find method for 'EnumOperation' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'EnumOperation' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllEnumOperations() {
        org.junit.Assert.assertNotNull("Data on demand for 'EnumOperation' failed to initialize correctly", dod.getRandomEnumOperation());
        long count = edu.purdue.cybercenter.dm.domain.EnumOperation.countEnumOperations();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'EnumOperation', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.EnumOperation> result = edu.purdue.cybercenter.dm.domain.EnumOperation.findAllEnumOperations();
        org.junit.Assert.assertNotNull("Find all method for 'EnumOperation' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'EnumOperation' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindEnumOperationEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'EnumOperation' failed to initialize correctly", dod.getRandomEnumOperation());
        long count = edu.purdue.cybercenter.dm.domain.EnumOperation.countEnumOperations();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.EnumOperation> result = edu.purdue.cybercenter.dm.domain.EnumOperation.findEnumOperationEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'EnumOperation' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'EnumOperation' returned an incorrect number of entries", count, result.size());
    }
}
