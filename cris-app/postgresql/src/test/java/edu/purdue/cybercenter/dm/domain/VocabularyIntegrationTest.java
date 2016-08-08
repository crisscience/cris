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
public class VocabularyIntegrationTest {

    @Test
    public void testMarkerMethod() {
    }

	@Autowired
    private VocabularyDataOnDemand dod;

	@Test
    public void testCountVocabularys() {
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to initialize correctly", dod.getRandomVocabulary());
        long count = edu.purdue.cybercenter.dm.domain.Vocabulary.countVocabularys();
        org.junit.Assert.assertTrue("Counter for 'Vocabulary' incorrectly reported there were no entries", count > 0);
    }

	@Test
    public void testFindVocabulary() {
        edu.purdue.cybercenter.dm.domain.Vocabulary obj = dod.getRandomVocabulary();
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Vocabulary.findVocabulary(id);
        org.junit.Assert.assertNotNull("Find method for 'Vocabulary' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Vocabulary' returned the incorrect identifier", id, obj.getId());
    }

	@Test
    public void testFindAllVocabularys() {
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to initialize correctly", dod.getRandomVocabulary());
        long count = edu.purdue.cybercenter.dm.domain.Vocabulary.countVocabularys();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Vocabulary', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<edu.purdue.cybercenter.dm.domain.Vocabulary> result = edu.purdue.cybercenter.dm.domain.Vocabulary.findAllVocabularys();
        org.junit.Assert.assertNotNull("Find all method for 'Vocabulary' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Vocabulary' failed to return any data", result.size() > 0);
    }

	@Test
    public void testFindVocabularyEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to initialize correctly", dod.getRandomVocabulary());
        long count = edu.purdue.cybercenter.dm.domain.Vocabulary.countVocabularys();
        if (count > 20) count = 20;
        java.util.List<edu.purdue.cybercenter.dm.domain.Vocabulary> result = edu.purdue.cybercenter.dm.domain.Vocabulary.findVocabularyEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Vocabulary' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Vocabulary' returned an incorrect number of entries", count, result.size());
    }

	@Test
    public void testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to initialize correctly", dod.getRandomVocabulary());
        edu.purdue.cybercenter.dm.domain.Vocabulary obj = dod.getNewTransientVocabulary(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Vocabulary' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Vocabulary' identifier to no longer be null", obj.getId());
    }

	@Test
    public void testRemove() {
        edu.purdue.cybercenter.dm.domain.Vocabulary obj = dod.getRandomVocabulary();
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to initialize correctly", obj);
        java.lang.Integer id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Vocabulary' failed to provide an identifier", id);
        obj = edu.purdue.cybercenter.dm.domain.Vocabulary.findVocabulary(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Vocabulary' with identifier '" + id + "'", edu.purdue.cybercenter.dm.domain.Vocabulary.findVocabulary(id));
    }
}
