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
import edu.purdue.cybercenter.dm.util.ServiceUtils;
import edu.purdue.cybercenter.dm.vocabulary.util.VocabularyUtils;
import edu.purdue.cybercenter.dm.xml.vocabulary.AttachTo;
import edu.purdue.cybercenter.dm.xml.vocabulary.Property;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 *
 * @author xu222
 */
@Service
public class TermService {

    static final private String VOCABULARY_SCHEMA_LOCATION = "http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0 vocabulary-1.0.0.xsd";

    private JAXBContext context;

    public TermService() {
        try {
            context = JAXBContext.newInstance(Term.class);
        } catch (JAXBException ex) {
            throw new RuntimeException("failed to instantiate unmarshaller for Term: " + ex.getMessage());
        }
    }

    @Autowired
    private DomainObjectService domainObjectService;

    public edu.purdue.cybercenter.dm.domain.Term saveXml(String xmlTerm, String filename, boolean isTemplate) {
        Term term = convertXmlToTerm(xmlTerm);
        return save(term, filename, isTemplate);
    }

    public edu.purdue.cybercenter.dm.domain.Term saveJson(String jsonTerm, String filename, boolean isTemplate) {
        Term term = Helper.deserialize(jsonTerm, Term.class);
        return save(term, filename, isTemplate);
    }

    public List<edu.purdue.cybercenter.dm.domain.Term> save(List<Term> terms, boolean isTemplate) {
        List<edu.purdue.cybercenter.dm.domain.Term> dbTerms = new ArrayList<>();
        for (Term term : terms) {
            edu.purdue.cybercenter.dm.domain.Term dbTerm = save(term, null, isTemplate);
            dbTerms.add(dbTerm);
        }

        return dbTerms;
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Term save(Term term, String filename, boolean isTemplate) {
        String sUuid = term.getUuid();
        String sVersionNumber = term.getVersion();
        if (sUuid == null || sVersionNumber == null) {
            throw new RuntimeException(String.format("A term cannot be saved with a null UUID: \"%s\" and/or version number: \"%s\"", sUuid, sVersionNumber));
        }

        UUID uuid = UUID.fromString(sUuid);
        UUID versionNumber = UUID.fromString(sVersionNumber);

        String key;
        if (filename == null || filename.isEmpty()) {
            key = Helper.replaceSpecialCharacters(term.getName()) + ".xml";
        } else {
            key = filename;
        }

        // check if the uuid and version number has been used
        edu.purdue.cybercenter.dm.domain.Term dbTerm = findByUuidAndVersionNumber(uuid, versionNumber);
        if (dbTerm != null && versionNumber != null) {
            // The term already exists
            if (!isLatest(term)) {
                dbTerm.setTimeUpdated(new Date());
                dbTerm = domainObjectService.merge(dbTerm, edu.purdue.cybercenter.dm.domain.Term.class);
            } else {
                throw new RuntimeException(String.format("The combination of UUID: \"%s\" and version: \"%s\" has been used by an existing term: ", sUuid, sVersionNumber));
            }
        } else {
            // It's a new term
            // check if a term of the same definition already exists
            dbTerm = new edu.purdue.cybercenter.dm.domain.Term();
            dbTerm.setUuid(uuid);
            dbTerm.setVersionNumber(versionNumber);
            dbTerm.setName(term.getName());
            dbTerm.setDescription(term.getDescription());
            dbTerm.setContent(convertTermToXml(term));
            dbTerm.setIsTemplate(isTemplate);
            dbTerm.setKey(key);
            domainObjectService.persist(dbTerm, edu.purdue.cybercenter.dm.domain.Term.class);
        }

        return dbTerm;
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Term makeTermLatest(Term term) {
        UUID uuid = UUID.fromString(term.getUuid());
        UUID version = UUID.fromString(term.getVersion());
        edu.purdue.cybercenter.dm.domain.Term dbTerm;
        if (uuid != null && version != null) {
            dbTerm = findByUuidAndVersionNumber(uuid, version);
            if (dbTerm != null) {
                dbTerm.setTimeUpdated(new Date());
                dbTerm = domainObjectService.merge(dbTerm, edu.purdue.cybercenter.dm.domain.Term.class);
            }
        } else {
            dbTerm = null;
        }
        return dbTerm;
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Term makeTermLatest(edu.purdue.cybercenter.dm.domain.Term dbTerm) {
        if (dbTerm != null) {
            dbTerm.setTimeUpdated(new Date());
            dbTerm = domainObjectService.merge(dbTerm, edu.purdue.cybercenter.dm.domain.Term.class);
        }
        return dbTerm;
    }

    public edu.purdue.cybercenter.dm.domain.Term findById(Integer id) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = domainObjectService.findById(id, edu.purdue.cybercenter.dm.domain.Term.class);
        return dbTerm;
    }

    public edu.purdue.cybercenter.dm.domain.Term findByUuidAndVersionNumber(UUID uuid, UUID versionNumber) {
        // if version is missing, get the latest version
        edu.purdue.cybercenter.dm.domain.Term dbTerm;
        TypedQuery<edu.purdue.cybercenter.dm.domain.Term> query;
        if (versionNumber != null) {
            query = DomainObjectHelper.createNamedQuery("Term.findByUuidAndVersionNumber", edu.purdue.cybercenter.dm.domain.Term.class);
            query.setParameter("versionNumber", versionNumber);
        } else {
            query = DomainObjectHelper.createNamedQuery("Term.findByUuid", edu.purdue.cybercenter.dm.domain.Term.class);
        }

        query.setParameter("uuid", uuid);
        query.setMaxResults(1);
        try {
            dbTerm = domainObjectService.executeTypedQueryWithSingleResult(query);
        } catch (EmptyResultDataAccessException ex) {
            dbTerm = null;
        }

        return dbTerm;
    }

    public List<edu.purdue.cybercenter.dm.domain.Term> findAllByUuid(UUID uuid) {
        // get all versions of the term
        TypedQuery<edu.purdue.cybercenter.dm.domain.Term> query = DomainObjectHelper.createNamedQuery("Term.findByUuid", edu.purdue.cybercenter.dm.domain.Term.class);
        query.setParameter("uuid", uuid);
        return domainObjectService.executeTypedQueryWithResultList(query);
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Term changeStatus(edu.purdue.cybercenter.dm.domain.Term dbTerm, EnumAssetStatus status) {
        if (dbTerm == null) {
            return null;
        }

        // if status is defined use it; otherwise flip the status of the term
        Integer newStatus;
        if (status != null) {
            newStatus = status.getIndex();
        } else {
            Integer currentStatus = dbTerm.getStatusId();
            if (currentStatus == 1) {
                newStatus = 0;
            } else {
                newStatus = 1;
            }
        }

        edu.purdue.cybercenter.dm.domain.Term updatedTermplate;
        UUID uuid = dbTerm.getUuid();
        if (newStatus == 1) {
            // restore: only the tip
            edu.purdue.cybercenter.dm.domain.Term dbt = findByUuidAndVersionNumber(uuid, null);
            dbt.setStatusId(EnumAssetStatus.Operational.getIndex());
            updatedTermplate = domainObjectService.merge(dbt, edu.purdue.cybercenter.dm.domain.Term.class);
        } else {
            // deprecate: all  versions
            TypedQuery<edu.purdue.cybercenter.dm.domain.Term> query = DomainObjectHelper.createNamedQuery("Term.findByUuid", edu.purdue.cybercenter.dm.domain.Term.class);
            query.setParameter("uuid", uuid);
            List<edu.purdue.cybercenter.dm.domain.Term> templates = domainObjectService.executeTypedQueryWithResultList(query);
            // to keep the relative oreder of timeUpdated
            Collections.reverse(templates);
            for (edu.purdue.cybercenter.dm.domain.Term v : templates) {
                v.setStatusId(newStatus);
                domainObjectService.flush(v, edu.purdue.cybercenter.dm.domain.Term.class);
            }
            updatedTermplate = templates.get(0);
        }

        return updatedTermplate;
    }

    public Term getTerm(UUID uuid, UUID versionNumber, boolean dereference) {
        Integer tenantId = edu.purdue.cybercenter.dm.threadlocal.TenantId.get();
        return getTerm(uuid, versionNumber, dereference, tenantId);
    }

    @Cacheable(value = "vocabulary")
    private Term getTerm(UUID uuid, UUID versionNumber, boolean dereference, Integer tenanId) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = findByUuidAndVersionNumber(uuid, versionNumber);
        Term term;
        if (dbTerm != null) {
            term = dbTermToTerm(dbTerm);
        } else {
            term = null;
        }

        if (dereference) {
            deReference(term);
        }

        return term;
    }

    public List<Term> getTerms(UUID uuid, boolean dereference) {
        // get all versions of the term
        List<edu.purdue.cybercenter.dm.domain.Term> dbTerms = findAllByUuid(uuid);

        List<Term> terms = new ArrayList<>();
        for (edu.purdue.cybercenter.dm.domain.Term dbTerm : dbTerms) {
            Term term = dbTermToTerm(dbTerm);
            if (dereference) {
                deReference(term);
            }
            terms.add(term);
        }

        return terms;
    }

    public String getXmlTerm(UUID uuid, UUID versionNumber, boolean dereference) {
        edu.purdue.cybercenter.dm.domain.Term dbTerm = findByUuidAndVersionNumber(uuid, versionNumber);

        String xmlTerm;
        if (dbTerm != null) {
            if (dereference) {
                Term term = dbTermToTerm(dbTerm);
                deReference(term);
                xmlTerm = convertTermToXml(term);
            } else {
                xmlTerm = dbTerm.getContent();
            }
        } else {
            xmlTerm = null;
        }

        return xmlTerm;
    }

    public List<String> getXmlTerms(UUID uuid) {
        List<edu.purdue.cybercenter.dm.domain.Term> dbTerms = findAllByUuid(uuid);

        List<String> xmlTerms = new ArrayList<>();
        for (edu.purdue.cybercenter.dm.domain.Term dbTerm : dbTerms) {
            xmlTerms.add(dbTerm.getContent());
        }

        return xmlTerms;
    }

    public void fixVersionNumber(Term term) {
        if (term == null) {
            return;
        }

        String version = term.getVersion();
        boolean isUsed = false;

        if (version == null || version.isEmpty()) {
            isUsed = true;
        } else {
            List<Term> terms = getTerms(UUID.fromString(term.getUuid()), false);
            for (Term t : terms) {
                if (version.equals(t.getVersion())) {
                    isUsed = true;
                    break;
                }
            }
        }

        if (isUsed) {
            term.setVersion(UUID.randomUUID().toString());
        }
    }

    public void fixVersionNumber(List<Term> terms) {
        for (Term term : terms) {
            fixVersionNumber(term);
        }
    }

    public String getType(Term term) {
        if (term == null) {
            return null;
        }

        String type = term.getType();

        if (type == null) {
            if (term.getValidation() != null) {
                List<ValidationType.Validator> validators = term.getValidation().getValidator();
                for (ValidationType.Validator validator : validators) {
                    type = validator.getType();
                    if (type != null) {
                        break;
                    }
                }
            }
        }

        return type;
    }

    public Term getSubTerm(Term term, String path) {
        if (term == null || path == null || path.trim().equals("")) {
            return term;
        }

        Term termFound = term;
        String[] aliases = path.split("\\.");
        for (String alias : aliases) {
            List<Term> terms = termFound.getTerm();
            termFound = null;

            Map<String, Object> result = ServiceUtils.processArrayNotation(alias);
            String base = (String) result.get("base");
            for (Term t : terms) {
                String termAlias = t.getAlias();
                if (termAlias == null || termAlias.isEmpty()) {
                    termAlias = t.getName();
                }

                if (termAlias != null && termAlias.equals(base)) {
                    termFound = t;
                    break;
                }
            }

            if (termFound == null) {
                break;
            }
        }

        return termFound;
    }

    public Boolean validateTerm(String xmlTerm) {
        Term term = convertXmlToTerm(xmlTerm);

        return validateTerm(term);
    }

    public Boolean validateTerm(Term term) {
        // this check if all the references are valid
        boolean isValid = true;

        String alias = term.getAlias();
        if (alias != null && !alias.isEmpty()) {
            // it's a reference
            isValid = termExists(term);
        } else {
            // it's a definition
            for (Term t : term.getTerm()) {
                isValid = validateTerm(t);
                if (!isValid) {
                    break;
                }
            }

            for (AttachTo a : term.getAttachTo()) {
                isValid = termExists(a.getUuid(), a.getVersion());
                if (!isValid) {
                    break;
                }
            }
        }

        return isValid;
    }

    public void doReference(Term term) {
        doReference(term, true);
    }

    public void doReference(Term term, boolean ignoreInvalid) {
        if (term == null) {
            return;
        }

        // remove any unnecessary info for reference field
        if (term.getAlias() != null) {
            UUID uuid = UUID.fromString(term.getUuid());
            UUID version = term.getVersion() == null ? null : UUID.fromString(term.getVersion());
            Term vTerm = getTerm(uuid, version, false);
            if (vTerm != null) {
                term.setName(null);
                term.setType(null);
                term.setUnit(null);
                term.setScale(null);
                term.setLength(null);
                term.getAttachTo().clear();
                term.getProperty().clear();

                // the following elements should only be removed if they are the same as the vocabulary term
                if (VocabularyUtils.isSame(term.getDescription(), vTerm.getDescription())) {
                    term.setDescription(null);
                }
                if (VocabularyUtils.isSame(term.getValue(), vTerm.getValue())) {
                    term.setValue(null);
                }
                if (VocabularyUtils.isSame(term.getShowExpression(), vTerm.getShowExpression())) {
                    term.setShowExpression(null);
                }
                if (VocabularyUtils.isSame(term.getValidation(), vTerm.getValidation())) {
                    term.setValidation(null);
                    term.getProperty().clear();
                }
                if (VocabularyUtils.isSame(term.isList(), vTerm.isList() == null ? false : vTerm.isList())) {
                    term.setList(null);
                }
                if (VocabularyUtils.isSame(term.isRequired(), vTerm.isRequired() == null ? false : vTerm.isRequired())) {
                    term.setRequired(null);
                }
                if (VocabularyUtils.isSame(term.getTerm(), vTerm.getTerm())) {
                    term.getTerm().clear();
                } else {
                    for (Term t : term.getTerm()) {
                        doReference(t, ignoreInvalid);
                    }
                }
            } else {
                // referenced something non-exist
                if (!ignoreInvalid) {
                    throw new RuntimeException(String.format("The referenced term with the following UUID and version does not exist: %s, %s", term.getUuid(), term.getVersion()));
                }
            }
        } else {
            for (Term t : term.getTerm()) {
                doReference(t, ignoreInvalid);
            }
        }

    }

    public void deReference(Term term) {
        deReference(term, true);
    }

    public void deReference(Term term, boolean ignoreInvalid) {
        if (term == null) {
            return;
        }

        UUID uuid = UUID.fromString(term.getUuid());
        UUID version = term.getVersion() == null ? null : UUID.fromString(term.getVersion());
        Term vTerm = getTerm(uuid, version, false);
        if (term.getAlias() == null) {
            // it's a definition itself, do nothing
        } else {
            // it's a reference
            if (vTerm != null) {
                term.setName(vTerm.getName());
                term.setType(vTerm.getType());
                term.setUnit(vTerm.getUnit());
                term.setScale(vTerm.getScale());
                term.setLength(vTerm.getLength());
                term.getProperty().clear();
                term.getProperty().addAll(vTerm.getProperty());
                term.getAttachTo().clear();
                term.getAttachTo().addAll(vTerm.getAttachTo());

                if (term.getVersion() == null) {
                    term.setVersion(vTerm.getVersion());
                }
                if (term.getTerm().isEmpty() || term.getTerm() == null) {
                    term.getTerm().clear();
                    term.getTerm().addAll(vTerm.getTerm());
                }
                if (term.isRequired() == null) {
                    term.setRequired(vTerm.isRequired());
                }
                if (term.isReadOnly() == null) {
                    term.setReadOnly(vTerm.isReadOnly());
                }
                if (term.isList() == null) {
                    term.setList(vTerm.isList());
                }
                if (term.getDescription() == null) {
                    term.setDescription(vTerm.getDescription());
                }
                if (term.getValue() == null) {
                    term.setValue(vTerm.getValue());
                }
                if (term.getShowExpression() == null) {
                    term.setShowExpression(vTerm.getShowExpression());
                }
                if (term.getValidation() == null) {
                    term.setValidation(vTerm.getValidation());
                }
            } else {
                // referenced something non-exist
                if (!ignoreInvalid) {
                    throw new RuntimeException(String.format("The referenced term with the following UUID and version does not exist: %s, %s", term.getUuid(), term.getVersion()));
                }
            }
        }

        for (Term t : term.getTerm()) {
            deReference(t, ignoreInvalid);
        }

        for (AttachTo a : term.getAttachTo()) {
            String sUuid = a.getUuid();
            String sVersion = a.getVersion();
            Term t = getTerm(UUID.fromString(sUuid), sVersion != null ? UUID.fromString(sVersion) : null, false);
            if (t == null) {
                if (!ignoreInvalid) {
                    throw new RuntimeException("The attached to term with UUID: " + sUuid + " does not exist");
                }
            } else {
                a.setVersion(t.getVersion());
            }
        }

        setDefaultProperties(term);
    }

    public boolean isTermInTerms(Term term, List<Term> terms) {
        boolean isTermInTerms = false;

        for (Term t : terms) {
            if (VocabularyUtils.isSame(term, t)) {
                isTermInTerms = true;
                break;
            }
        }

        return isTermInTerms;
    }

    public boolean isDefined(Term term) {
        boolean isDefined = false;

        List<Term> terms = getTerms(UUID.fromString(term.getUuid()), false);
        for (Term t : terms) {
            if (VocabularyUtils.isSame(term, t)) {
                isDefined = true;
                break;
            }
        }

        return isDefined;
    }

    /*
     * term needs to be the de-referenced version
     */
    public Term getExistingTerm(Term term) {
        Term existingTerm = null;

        if (term != null && term.getUuid() != null && !term.getUuid().isEmpty()) {
            List<Term> terms = getTerms(UUID.fromString(term.getUuid()), false);
            for (Term t : terms) {
                try {
                    deReference(t);
                } catch (Exception ex) {
                    // if there's any problem with the template to be compared with
                    // treat it as a different template
                    continue;
                }

                if (VocabularyUtils.isSame(term, t)) {
                    existingTerm = t;
                    break;
                }
            }
        }

        return existingTerm;
    }

    public boolean isTermValid(Term term) {
        boolean isTermValid;
        if (StringUtils.isEmpty(term.getAlias())) {
            isTermValid = true;
        } else {
            edu.purdue.cybercenter.dm.domain.Term found  = findByUuidAndVersionNumber(UUID.fromString(term.getUuid()), null);
            isTermValid = found != null;
        }
        return isTermValid;
    }

    public boolean isTermValid(AttachTo attachTo) {
        edu.purdue.cybercenter.dm.domain.Term found  = findByUuidAndVersionNumber(UUID.fromString(attachTo.getUuid()), null);

        boolean isTermValid = found != null;

        return isTermValid;
    }

    public boolean isVersionValid(Term term) {
        boolean isVersionValid;

        if (StringUtils.isEmpty(term.getAlias())) {
            isVersionValid = true;
        } else {
            String uuid = term.getUuid();
            String version = term.getVersion();
            if (!StringUtils.isEmpty(uuid) && !StringUtils.isEmpty(version)) {
                edu.purdue.cybercenter.dm.domain.Term found  = findByUuidAndVersionNumber(UUID.fromString(uuid), UUID.fromString(version));
                isVersionValid = found != null;
            } else {
                isVersionValid = false;
            }
        }

        return isVersionValid;
    }

    public boolean isVersionValid(AttachTo attachTo) {
        boolean isVersionValid = false;

        String uuid = attachTo.getUuid();
        String version = attachTo.getVersion();
        if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(version)) {
            return isVersionValid;
        }

        edu.purdue.cybercenter.dm.domain.Term found  = findByUuidAndVersionNumber(UUID.fromString(uuid), UUID.fromString(version));

        isVersionValid = found != null;

        return isVersionValid;
    }

    public boolean isLatest(Term term) {
        // "term" needs to be an existing in the system
        boolean isLatest = true;

        edu.purdue.cybercenter.dm.domain.Term latestTerm = findByUuidAndVersionNumber(UUID.fromString(term.getUuid()), null);
        if (latestTerm != null) {
            String version = latestTerm.getVersionNumber() != null ? latestTerm.getVersionNumber().toString() : null;
            if (version != null && !version.equals(term.getVersion())) {
                isLatest = false;
            }
        }

        return isLatest;
    }

    public boolean isLatest(AttachTo attachTo) {
        // "term" needs to be an existing in the system
        boolean isLatest = true;

        edu.purdue.cybercenter.dm.domain.Term latestTerm = findByUuidAndVersionNumber(UUID.fromString(attachTo.getUuid()), null);
        if (latestTerm != null) {
            String version = latestTerm.getVersionNumber() != null ? latestTerm.getVersionNumber().toString() : null;
            if (version != null && !version.equals(attachTo.getVersion())) {
                isLatest = false;
            }
        }

        return isLatest;
    }

    public boolean termExists(String sUuid, String sVersion) {
        UUID uuid = UUID.fromString(sUuid);
        UUID version = UUID.fromString(sVersion);
        return termExists(uuid, version);
    }

    public boolean termExists(UUID uuid, UUID version) {
        boolean termExists;

        if (uuid == null || version == null) {
            termExists = false;
        } else {
            edu.purdue.cybercenter.dm.domain.Term dbTerm = findByUuidAndVersionNumber(uuid, version);
            termExists = dbTerm != null;
        }

        return termExists;
    }

    public boolean termExists(Term term) {
        boolean termExists;

        try {
            UUID uuid = UUID.fromString(term.getUuid());
            UUID version = UUID.fromString(term.getVersion());
            termExists = termExists(uuid, version);
        } catch (Exception ex) {
            termExists = false;
        }

        return termExists;
    }

    public Term dbTermToTerm(edu.purdue.cybercenter.dm.domain.Term dbTerm) {
        String xmlTerm = dbTerm.getContent();
        Term term = convertXmlToTerm(xmlTerm);
        return term;
    }

    public edu.purdue.cybercenter.dm.domain.Term termToDbTerm(Term term) {
        UUID uuid = UUID.fromString(term.getUuid());
        UUID versionNumber = UUID.fromString(term.getVersion());
        edu.purdue.cybercenter.dm.domain.Term dbTerm = findByUuidAndVersionNumber(uuid, versionNumber);
        return dbTerm;
    }

    public List<Term> dbTermsToTerms(List<edu.purdue.cybercenter.dm.domain.Term> dbTerms) {
        List<Term> terms = new ArrayList<>();
        for (edu.purdue.cybercenter.dm.domain.Term dbTerm : dbTerms) {
            terms.add(dbTermToTerm(dbTerm));
        }
        return terms;
    }

    public List<edu.purdue.cybercenter.dm.domain.Term> termsToDbTerms(List<Term> terms) {
        List<edu.purdue.cybercenter.dm.domain.Term> dbTerms = new ArrayList<>();
        for (Term term : terms) {
            dbTerms.add(termToDbTerm(term));
        }
        return dbTerms;
    }

    public Term convertXmlToTerm(String xmlTerm) {
        Term term = null;
        try (InputStream is = new ByteArrayInputStream(xmlTerm.getBytes())) {
            Unmarshaller unMarshaller = context.createUnmarshaller();
            term = (Term) unMarshaller.unmarshal(is);
            fixExpression(term);
        } catch (JAXBException ex) {
            throw new RuntimeException("Unable to convert xml to term definition", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to read xml term definition", ex);
        }
        return term;
    }

    public String convertTermToXml(Term term) {
        String xmlTerm = null;
        try (OutputStream os = new ByteArrayOutputStream()) {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(CharacterEscapeHandler.class.getName(), CDataEscapeHandler.theInstance);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, VOCABULARY_SCHEMA_LOCATION);
            marshaller.marshal(term, os);
            xmlTerm = os.toString();
        } catch (JAXBException ex) {
            throw new RuntimeException("Unable to generate xml term definition", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to write xml term definition", ex);
        }
        return xmlTerm;
    }

    public boolean fileIsList(Term term) {
        boolean isList = false;

        ValidationType validation = term.getValidation();
        if (validation != null) {
            List<ValidationType.Validator> validators = validation.getValidator();
            if (!validators.isEmpty()) {
                ValidationType.Validator validator = validators.get(0);
                List<Property> properties = validator.getProperty();
                for (Property property : properties) {
                    if ("multiple".equals(property.getName())) {
                        if ("true".equals(property.getValue())) {
                            isList = true;
                        }
                        break;
                    }
                }
            }
        }
        return isList;
    }

    private void setDefaultProperties(Term term) {
        VocabularyUtils.fixTerm(term);
    }

    private void fixExpression(Term term) {
        String readOnlyExpression = term.getReadOnlyExpressionAttr();
        String requiredExpression = term.getRequiredExpressionAttr();
        if (StringUtils.isNotEmpty(readOnlyExpression)) {
            term.setReadOnlyExpression(readOnlyExpression);
        }
        if (StringUtils.isNotEmpty(requiredExpression)) {
            term.setRequiredExpression(requiredExpression);
        }

        term.getAttachTo().stream().forEach((a) -> {
            String readOnly = a.getReadOnlyExpressionAttr();
            String required = a.getRequiredExpressionAttr();
            if (StringUtils.isNotEmpty(readOnly)) {
                a.setReadOnlyExpression(readOnly);
            }
            if (StringUtils.isNotEmpty(required)) {
                a.setRequiredExpression(required);
            }
        });

        term.getTerm().stream().forEach((t) -> {
            fixExpression(t);
        });

    }
}
