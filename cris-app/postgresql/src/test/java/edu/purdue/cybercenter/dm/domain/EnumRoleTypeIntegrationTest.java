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
public class EnumRoleTypeIntegrationTest {

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
    private EnumRoleTypeDataOnDemand dod;

	@Test
    public void testCountEnumRoleTypes() {
        org.junit.Assert.assertNotNull("Data on demand for 'EnumRoleType' failed to initialize correctly", dod.getRandomEnumRoleType());
        long count = edu.purdue.cybercenter.dm.domain.EnumRoleType.countEnumRoleTypes();
        org.junit.Assert.assertTrue("Counter for 'EnumRoleType' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindEnumRoleType() {
        edu.purdue.cybercenter.dm.domain.EnumRoleType obj = dod.getRandomEnumRoleType();
        org.junit.Assert.assertNotNull("Data on demand for 'EnumRoleType' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'EnumRoleType' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.EnumRoleType.findEnumRoleType(id);
        org.junit.Assert.assertNotNull("Find method for 'EnumRoleType' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'EnumRoleType' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllEnumRoleTypes() {
        org.junit.Assert.assertNotNull("Data on demand for 'EnumRoleType' failed to initialize correctly", dod.getRandomEnumRoleType());
        long count = edu.purdue.cybercenter.dm.domain.EnumRoleType.countEnumRoleTypes();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'EnumRoleType', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.EnumRoleType> result = edu.purdue.cybercenter.dm.domain.EnumRoleType.findAllEnumRoleTypes();
        org.junit.Assert.assertNotNull("Find all method for 'EnumRoleType' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'EnumRoleType' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindEnumRoleTypeEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'EnumRoleType' failed to initialize correctly", dod.getRandomEnumRoleType());
        long count = edu.purdue.cybercenter.dm.domain.EnumRoleType.countEnumRoleTypes();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.EnumRoleType> result = edu.purdue.cybercenter.dm.domain.EnumRoleType.findEnumRoleTypeEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'EnumRoleType' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'EnumRoleType' returned an incorrect number of entries", count, result.size());
    }
}
