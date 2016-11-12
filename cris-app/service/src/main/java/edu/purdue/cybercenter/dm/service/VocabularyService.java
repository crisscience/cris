/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import edu.purdue.cybercenter.dm.util.CDataEscapeHandler;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.EnumAssetStatus;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.vocabulary.util.VocabularyUtils;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.Vocabulary;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class VocabularyService {

    private static final String KEY_TERMS_NEW = "new";
    private static final String KEY_TERMS_UPDATED = "updated";
    private static final String KEY_TERMS_DELETED = "deleted";

    private static final String VOCABULARY_SCHEMA_LOCATION = "http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd";

    private JAXBContext context;

    public VocabularyService() {
        try {
            context = JAXBContext.newInstance(Vocabulary.class);
        } catch (JAXBException ex) {
            throw new RuntimeException("failed to instantiate unmarshaller for Vocabulary: " + ex.getMessage());
        }
    }

    @Autowired
    private DomainObjectService domainObjectService;
    @Autowired
    private TermService termService;

    private void fixTerms(Vocabulary vocabulary) {
        // to be compatible to the old way of storing terms
        if (vocabulary != null && vocabulary.getTerms().getTerm().isEmpty()) {
            UUID uuid = UUID.fromString(vocabulary.getUuid());
            UUID versionNumber = UUID.fromString(vocabulary.getVersion());
            edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = findByUuidAndVersion(uuid, versionNumber);
            List<edu.purdue.cybercenter.dm.domain.Term> dbTerms = dbVocabulary.getTerms();
            vocabulary.getTerms().getTerm().addAll(termService.dbTermsToTerms(dbTerms));
        }
    }

    private void fixVersion(Vocabulary vocabulary) {
        String version = vocabulary.getVersion();
        boolean isUsed = false;

        if (StringUtils.isBlank(version)) {
            isUsed = true;
        } else {
            List<edu.purdue.cybercenter.dm.domain.Vocabulary> allVersions = findByUuid(UUID.fromString(vocabulary.getUuid()));
            for (edu.purdue.cybercenter.dm.domain.Vocabulary v : allVersions) {
                if (version.equals(v.getVersionNumber().toString())) {
                    isUsed = true;
                    break;
                }
            }
        }

        if (isUsed) {
            vocabulary.setVersion(UUID.randomUUID().toString());
        }
    }

    private boolean isDefinitionChanged(Vocabulary vocabulary) {
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = findLatestByUuid(UUID.fromString(vocabulary.getUuid()));
        if (dbVocabulary != null) {
            // an existing vocabulary
            Vocabulary latestVocabulary = dbVocabularyToVocabulary(dbVocabulary);
            return !VocabularyUtils.isSame(latestVocabulary, vocabulary);
        } else {
            // a new vocabulary
            return true;
        }
    }

    private Map<String, List<Term>> getChangedTerms(Vocabulary vocabulary) {
        edu.purdue.cybercenter.dm.domain.Vocabulary latestDbVocabulary = findLatestByUuid(UUID.fromString(vocabulary.getUuid()));
        Vocabulary latestVocabulary;
        if (latestDbVocabulary != null) {
            latestVocabulary = dbVocabularyToVocabulary(latestDbVocabulary);
        } else {
            latestVocabulary = null;
        }

        List<Term> newTerms = new ArrayList<>();
        List<Term> updatedTerms = new ArrayList<>();
        List<Term> deletedTerms = new ArrayList<>();

        if (latestVocabulary != null) {
            // existing vocabulary
            // figure out new and updated terms
            for (Term term : vocabulary.getTerms().getTerm()) {
                boolean isNew = true;
                boolean isUpdated = true;
                for (Term latestTerm : latestVocabulary.getTerms().getTerm()) {
                    if (latestTerm.getUuid().equals(term.getUuid())) {
                        isNew = false;
                        if (VocabularyUtils.isSame(latestTerm, term)) {
                            isUpdated = false;
                        }
                    }
                }
                if (isNew) {
                    newTerms.add(term);
                } else if (isUpdated) {
                    updatedTerms.add(term);
                }
            }

            // figure out deleted terms
            for (Term latestTerm : latestVocabulary.getTerms().getTerm()) {
                boolean exist = false;
                for (Term term : vocabulary.getTerms().getTerm()) {
                    if (latestTerm.getUuid().equals(term.getUuid())) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    deletedTerms.add(latestTerm);
                }
            }
        } else {
            // new vocabulary
            newTerms.addAll(vocabulary.getTerms().getTerm());
        }

        Map<String, List<Term>> changedTerms = new HashMap<>();
        changedTerms.put(KEY_TERMS_NEW, newTerms);
        changedTerms.put(KEY_TERMS_UPDATED, updatedTerms);
        changedTerms.put(KEY_TERMS_DELETED, deletedTerms);

        return changedTerms;
    }

    public edu.purdue.cybercenter.dm.domain.Vocabulary saveXml(String xmlVocabulary, String filename) {
        Vocabulary vocabulary = convertXmlToVocabulary(xmlVocabulary);
        return save(vocabulary, filename);
    }

    public edu.purdue.cybercenter.dm.domain.Vocabulary saveJson(String jsonVocabulary, String filename) {
        Vocabulary vocabulary = Helper.deserialize(jsonVocabulary, Vocabulary.class);
        return save(vocabulary, filename);
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Vocabulary save(Vocabulary vocabulary, String filename) {
        // pre-processing
        fixVersion(vocabulary);
        List<Term> terms = vocabulary.getTerms().getTerm();
        for (Term term: terms) {
            VocabularyUtils.fixTerm(term);
        }

        Map<String, List<Term>> changedTerms = getChangedTerms(vocabulary);
        List<Term> newTerms = changedTerms.get(KEY_TERMS_NEW);
        List<Term> updatedTerms = changedTerms.get(KEY_TERMS_UPDATED);
        List<Term> deletedTerms = changedTerms.get(KEY_TERMS_DELETED);
        boolean isTermChanged = !newTerms.isEmpty() || !updatedTerms.isEmpty() || !deletedTerms.isEmpty();
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary;
        if (isDefinitionChanged(vocabulary) || isTermChanged) {
            // Save the list of new terms
            if (!newTerms.isEmpty()) {
                termService.fixVersionNumber(newTerms);
                termService.save(newTerms, false);
            }

            // Save the list of updated terms
            if (!updatedTerms.isEmpty()) {
                termService.fixVersionNumber(updatedTerms);
                termService.save(updatedTerms, false);
            }

            // deprecate the deleted terms
            if (!deletedTerms.isEmpty()) {
                for (Term term : deletedTerms) {
                    edu.purdue.cybercenter.dm.domain.Term dbTerm = termService.termToDbTerm(term);
                    termService.changeStatus(dbTerm, EnumAssetStatus.Deprecated);
                }
            }

            // Save vocabulary definition
            String key;
            if (StringUtils.isBlank(filename)) {
                key = Helper.replaceSpecialCharacters(vocabulary.getName()) + ".xml";
            } else {
                key = filename;
            }
            String xmlUuid = vocabulary.getUuid();
            String xmlVersion = vocabulary.getVersion();
            String xmlName = vocabulary.getName();
            String xmlDescription = vocabulary.getDescription();
            String xmlDomain = vocabulary.getDomain();
            List<edu.purdue.cybercenter.dm.domain.Term> dbTerms = termService.termsToDbTerms(terms);

            dbVocabulary = new edu.purdue.cybercenter.dm.domain.Vocabulary();
            dbVocabulary.setUuid(UUID.fromString(xmlUuid));
            dbVocabulary.setVersionNumber(UUID.fromString(xmlVersion));
            dbVocabulary.setKey(key);
            dbVocabulary.setName(xmlName);
            dbVocabulary.setDescription(xmlDescription);
            dbVocabulary.setDomain(xmlDomain);
            dbVocabulary.setContent(convertVocabularyToXml(vocabulary));
            dbVocabulary.setTerms(dbTerms);
            domainObjectService.persist(dbVocabulary, edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        } else {
            dbVocabulary = null;
        }

        return dbVocabulary;
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Vocabulary makeVocabularyLatest(Vocabulary vocabulary) {
        UUID uuid = UUID.fromString(vocabulary.getUuid());
        UUID version = UUID.fromString(vocabulary.getVersion());
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary;
        if (uuid != null && version != null) {
            dbVocabulary = findByUuidAndVersion(uuid, version);
            dbVocabulary = makeVocabularyLatest(dbVocabulary);
        } else {
            dbVocabulary = null;
        }
        return dbVocabulary;
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Vocabulary makeVocabularyLatest(edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary) {
        if (dbVocabulary != null) {
            dbVocabulary.setTimeUpdated(new Date());
            dbVocabulary = domainObjectService.merge(dbVocabulary, edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        }
        return dbVocabulary;
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Vocabulary changeStatus(edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary, EnumAssetStatus status) {
        if (dbVocabulary == null) {
            return null;
        }

        Integer currentStatusIndex = dbVocabulary.getStatusId();
        EnumAssetStatus currentStatus = (currentStatusIndex != null && currentStatusIndex == 1 ? EnumAssetStatus.Operational : EnumAssetStatus.Deprecated);

        // if status is defined use it; otherwise flip the status of the vocabulary
        EnumAssetStatus newStatus;
        if (status != null) {
            newStatus = status;
        } else {
            if (currentStatus.equals(EnumAssetStatus.Operational)) {
                newStatus = EnumAssetStatus.Deprecated;
            } else {
                newStatus = EnumAssetStatus.Operational;
            }
        }

        List<edu.purdue.cybercenter.dm.domain.Term> dbTerms = dbVocabulary.getTerms();
        for (edu.purdue.cybercenter.dm.domain.Term dbTerm : dbTerms) {
            dbTerm.setStatusId(newStatus.getIndex());
        }
        dbVocabulary.setStatusId(newStatus.getIndex());
        dbVocabulary.flush();

        return dbVocabulary;
    }

    public String export(edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary) {
        Vocabulary vocabulary = dbVocabularyToVocabulary(dbVocabulary);
        String xmlVocabulary = convertVocabularyToXml(vocabulary);
        return xmlVocabulary;
    }

    /**
     * Get an XML version of a vocabulary from storage
     *
     * @param id: the id of the vocabulary
     * @return: a vocabulary including just the most recent version of a term
     */
    public String export(Integer id) {
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = domainObjectService.findById(id, edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        String xmlVocabulary = export(dbVocabulary);
        return xmlVocabulary;
    }

    /**
     * Get an XML version of a vocabulary from storage
     *
     * @param uuid: the UUID of the vocabulary
     * @param versionNumber: the version of the vocabulary
     * @return: a vocabulary including just the most recent version of a term
     */
    public String export(UUID uuid, UUID versionNumber) {
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = findByUuidAndVersion(uuid, versionNumber);
        String xmlVocabulary = export(dbVocabulary);
        return xmlVocabulary;
    }

    /**
     * Convert XML vocabulary to a vocabulary object
     *
     * @param xmlVocabulary: the XML version of a vocabulary
     * @return: a vocabulary object
     */
    public Vocabulary convertXmlToVocabulary(String xmlVocabulary) {
        Vocabulary vocabulary = null;
        try (InputStream is = new ByteArrayInputStream(xmlVocabulary.getBytes())) {
            Unmarshaller unMarshaller = context.createUnmarshaller();
            vocabulary = (Vocabulary) unMarshaller.unmarshal(is);
        } catch (JAXBException ex) {
            throw new RuntimeException("Unable to generate xml term definition", ex);
        } catch (IOException ex) {
            throw new RuntimeException("UUnable to read xml vocabulary definition", ex);
        }
        return vocabulary;
    }

    /**
     * Convert a vocabulary object to Xml vocabulary
     *
     * @param vocabulary: a vocabulary object
     * @return: an XML version of the vocabulary
     */
    public String convertVocabularyToXml(Vocabulary vocabulary) {
        String xmlVocabulary = null;
        try (OutputStream os = new ByteArrayOutputStream()) {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(CharacterEscapeHandler.class.getName(), CDataEscapeHandler.theInstance);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, VOCABULARY_SCHEMA_LOCATION);
            marshaller.marshal(vocabulary, os);
            xmlVocabulary = os.toString();
        } catch (JAXBException ex) {
            throw new RuntimeException("Unable to generate xml vocabulary definition", ex);
        } catch (IOException ex) {
            throw new RuntimeException("UUnable to write xml vocabulary definition", ex);
        }
        return xmlVocabulary;
    }

    public edu.purdue.cybercenter.dm.domain.Vocabulary findById(Integer id) {
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary = domainObjectService.findById(id, edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        return dbVocabulary;
    }

    public edu.purdue.cybercenter.dm.domain.Vocabulary findByUuidAndVersion(UUID uuid, UUID version) {
        TypedQuery<edu.purdue.cybercenter.dm.domain.Vocabulary> query;
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary;
        if (version != null) {
            query = DomainObjectHelper.createNamedQuery("Vocabulary.findByUuidAndVersionNumber", edu.purdue.cybercenter.dm.domain.Vocabulary.class);
            query.setParameter("versionNumber", version);
        } else {
            query = DomainObjectHelper.createNamedQuery("Vocabulary.findByUuid", edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        }

        query.setParameter("uuid", uuid);
        query.setMaxResults(1);
        try {
            dbVocabulary = domainObjectService.executeTypedQueryWithSingleResult(query);
        } catch (EmptyResultDataAccessException ex) {
            dbVocabulary = null;
        }

        return dbVocabulary;
    }

    public edu.purdue.cybercenter.dm.domain.Vocabulary findLatestByUuid(UUID uuid) {
        TypedQuery<edu.purdue.cybercenter.dm.domain.Vocabulary> query = DomainObjectHelper.createNamedQuery("Vocabulary.findByUuid", edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        query.setParameter("uuid", uuid);
        query.setMaxResults(1);
        edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary;
        try {
            dbVocabulary = domainObjectService.executeTypedQueryWithSingleResult(query);
        } catch (EmptyResultDataAccessException ex) {
            dbVocabulary = null;
        }
        return dbVocabulary;
    }

    public List<edu.purdue.cybercenter.dm.domain.Vocabulary> findByUuid(UUID uuid) {
        TypedQuery<edu.purdue.cybercenter.dm.domain.Vocabulary> query = DomainObjectHelper.createNamedQuery("Vocabulary.findByUuid", edu.purdue.cybercenter.dm.domain.Vocabulary.class);
        query.setParameter("uuid", uuid);
        return domainObjectService.executeTypedQueryWithResultList(query);
    }

    public Vocabulary dbVocabularyToVocabulary(edu.purdue.cybercenter.dm.domain.Vocabulary dbVocabulary) {
        String xmlVocabulary = dbVocabulary.getContent();
        Vocabulary vocabulary = convertXmlToVocabulary(xmlVocabulary);
        fixTerms(vocabulary);
        return vocabulary;
    }

    public boolean isLatest(Vocabulary vocabulary) {
        if (vocabulary == null) {
            return false;
        }

        UUID uuid = UUID.fromString(vocabulary.getUuid());
        edu.purdue.cybercenter.dm.domain.Vocabulary latestDbVocabulary = findLatestByUuid(uuid);
        if (latestDbVocabulary == null) {
            return false;
        }
        String latestVersion = latestDbVocabulary.getVersionNumber().toString();

        return vocabulary.getVersion().equals(latestVersion);
    }

}
