/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import edu.purdue.cybercenter.dm.util.Helper;
import edu.purdue.cybercenter.dm.vocabulary.util.VocabularyUtils;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import java.util.List;
import java.util.UUID;
import javax.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 *
 * @author xu222
 */
@Service
public class TemplateService {

    @Autowired
    private TermService termService;

    public List<edu.purdue.cybercenter.dm.domain.Term> findAll() {
        TypedQuery<edu.purdue.cybercenter.dm.domain.Term> query = DomainObjectHelper.createNamedQuery("Term.findTemplateAll", edu.purdue.cybercenter.dm.domain.Term.class);
        return query.getResultList();
    }

    public List<edu.purdue.cybercenter.dm.domain.Term> findLatest() {
        TypedQuery<edu.purdue.cybercenter.dm.domain.Term> query = DomainObjectHelper.createNamedQuery("Term.findTemplateLatest", edu.purdue.cybercenter.dm.domain.Term.class);
        return query.getResultList();
    }

    public List<edu.purdue.cybercenter.dm.domain.Term> findAllByUuid(UUID uuid) {
        TypedQuery<edu.purdue.cybercenter.dm.domain.Term> query = DomainObjectHelper.createNamedQuery("Term.findTemplateAllByUuid", edu.purdue.cybercenter.dm.domain.Term.class);
        query.setParameter("uuid", uuid);
        return query.getResultList();
    }

    public edu.purdue.cybercenter.dm.domain.Term findLatestByUuid(UUID uuid) {
        TypedQuery<edu.purdue.cybercenter.dm.domain.Term> query = DomainObjectHelper.createNamedQuery("Term.findTemplateLatestByUuid", edu.purdue.cybercenter.dm.domain.Term.class);
        query.setParameter("uuid", uuid);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    public edu.purdue.cybercenter.dm.domain.Term findByUuidAndVersionNumber(UUID uuid, UUID versionNumber) {
        TypedQuery<edu.purdue.cybercenter.dm.domain.Term> query = DomainObjectHelper.createNamedQuery("Term.findTemplateByUuidAndVersionNumber", edu.purdue.cybercenter.dm.domain.Term.class);
        query.setParameter("uuid", uuid);
        query.setParameter("versionNumber", versionNumber);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    public edu.purdue.cybercenter.dm.domain.Term saveXml(String xmlTemplate, String filename) {
        Term template = termService.convertXmlToTerm(xmlTemplate);
        return save(template, filename);
    }

    public edu.purdue.cybercenter.dm.domain.Term saveJson(String jsonTemplate, String filename) {
        Term template = Helper.deserialize(jsonTemplate, Term.class);
        return save(template, filename);
    }

    @CacheEvict(value = "vocabulary", allEntries = true)
    public edu.purdue.cybercenter.dm.domain.Term save(Term template, String filename) {
        termService.deReference(template);
        VocabularyUtils.fixTerm(template);

        Term existingTemplate = termService.getExistingTerm(template);
        edu.purdue.cybercenter.dm.domain.Term dbTemplate;
        if (existingTemplate != null) {
            String message;
            if (termService.isLatest(existingTemplate)) {
                dbTemplate = termService.termToDbTerm(existingTemplate);
                message = "Template: " + template.getName() + " already exists and is the latest version";
                throw new RuntimeException(message);
            } else {
                edu.purdue.cybercenter.dm.domain.Term dbTerm = termService.makeTermLatest(existingTemplate);
                if (dbTerm != null) {
                    dbTemplate = dbTerm;
                    message = "Template: an older version of " + template.getName() + " already exists and has been made the latest version";
                } else {
                    message = "Template: an older version of " + template.getName() + " already exists but failed to make it the latest version";
                    throw new RuntimeException(message);
                }
            }
        } else {
            termService.doReference(template);
            termService.fixVersionNumber(template);
            dbTemplate = termService.save(template, StringUtils.isEmpty(filename) ? template.getName() : filename, true);
        }

        return dbTemplate;
    }
}
