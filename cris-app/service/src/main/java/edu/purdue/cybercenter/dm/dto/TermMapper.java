/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.purdue.cybercenter.dm.dto;

import edu.purdue.cybercenter.dm.service.TermService;
import edu.purdue.cybercenter.dm.service.VocabularyService;
import edu.purdue.cybercenter.dm.vocabulary.util.VocabularyUtils;
import edu.purdue.cybercenter.dm.xml.vocabulary.AttachTo;
import edu.purdue.cybercenter.dm.xml.vocabulary.Term;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType;
import edu.purdue.cybercenter.dm.xml.vocabulary.ValidationType.Validator;
import edu.purdue.cybercenter.dm.xml.vocabulary.Vocabulary;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Configurable;

/**
 *
 * @author xu222
 */
@Mapper
@Configurable
public abstract class TermMapper {

    @Inject
    TermService termService;

    @Inject
    VocabularyService vocabularyService;

    public static final TermMapper INSTANCE = Mappers.getMapper(TermMapper.class);

    @Mappings({
        @Mapping(target = "latestVersion", expression = "java(getLatestVersion(vocabulary))"),
        @Mapping(target = "isLatest", expression = "java(isLatest(vocabulary))")
    })
    public abstract VocabularyDto toVocabularyDto(Vocabulary vocabulary);

    public abstract List<VocabularyDto> toVocabularyDtos(List<Vocabulary> vocabularys);

    public abstract VocabularyDto.InheritedFrom toVocabularyInheritedFromDto(Vocabulary.InheritedFrom vocabularyInheritedFrom);

    public abstract VocabularyDto.InheritedFrom.VocabularyRef toVocabularyInheritedFromVocabularyRefDto(Vocabulary.InheritedFrom.VocabularyRef vocabularyInheritedFromVocabularyRef);

    public abstract VocabularyDto.Contributors toVocabularyContributorsDto(Vocabulary.Contributors vocabularyContributors);

    public abstract VocabularyDto.Terms toVocabularyTermsDto(Vocabulary.Terms vocabularyTerms);

    @Mappings({
        @Mapping(target = "validation", expression = "java(fixValidation(term))"),
        @Mapping(target = "list", expression = "java(isList(term))"),
        @Mapping(target = "latestVersion", expression = "java(getLatestVersion(term))"),
        @Mapping(target = "isLatest", expression = "java(isLatest(term))"),
        @Mapping(target = "isVersionValid", expression = "java(isVersionValid(term))"),
        @Mapping(target = "isTermValid", expression = "java(isTermValid(term))")
    })
    public abstract TermDto toTermDto(Term term);

    public abstract List<TermDto> toTermDtos(List<Term> terms);

    @Mappings({
        @Mapping(target = "latestVersion", expression = "java(getLatestVersion(attachTo))"),
        @Mapping(target = "isLatest", expression = "java(isLatest(attachTo))"),
        @Mapping(target = "isVersionValid", expression = "java(isVersionValid(attachTo))"),
        @Mapping(target = "isTermValid", expression = "java(isTermValid(attachTo))")
    })
    public abstract AttachToDto toAttachToDto(AttachTo attachTo);

    public abstract List<AttachToDto> toAttachToDtos(List<AttachTo> attachTos);

    protected String getLatestVersion(Vocabulary vocabulary) {
        if (vocabulary == null) {
            return null;
        }

        UUID uuid = UUID.fromString(vocabulary.getUuid());
        Vocabulary latestVocabulary = vocabularyService.dbVocabularyToVocabulary(vocabularyService.findLatestByUuid(uuid));

        String version;
        if (latestVocabulary != null) {
            version = latestVocabulary.getVersion();
        } else {
            version = null;
        }

        return version;
    }

    protected Boolean isList(Term term) {
        Boolean isList = term.isList();

        ValidationType validation = term.getValidation();
        if (validation != null) {
            List<Validator> validators = term.getValidation().getValidator();
            if (!validators.isEmpty()) {
                Validator validator = validators.get(0);
                String type = validator.getType();
                if (type != null && type.equals("file")) {
                    isList = null;
                }
            }
        }

        return isList;
    }

    protected ValidationType fixValidation(Term term) {
        return VocabularyUtils.fixValidation(term);
    }

    protected String getLatestVersion(Term term) {
        if (term == null) {
            return null;
        }

        UUID uuid = UUID.fromString(term.getUuid());
        String version = getLatestVersion(uuid);

        return version == null ? term.getVersion() : version;
    }

    protected String getLatestVersion(AttachTo attachTo) {
        if (attachTo == null) {
            return null;
        }

        UUID uuid = UUID.fromString(attachTo.getUuid());
        String version = getLatestVersion(uuid);

        return version == null ? attachTo.getVersion() : version;
    }

    protected Boolean isLatest(Vocabulary vocabulary) {
        return vocabularyService.isLatest(vocabulary);
    }

    protected Boolean isLatest(Term term) {
        return termService.isLatest(term);
    }

    protected Boolean isLatest(AttachTo attachTo) {
        return termService.isLatest(attachTo);
    }

    protected Boolean isTermValid(Term term) {
        return termService.isTermValid(term);
    }

    protected Boolean isTermValid(AttachTo attachTo) {
        return termService.isTermValid(attachTo);
    }

    protected Boolean isVersionValid(Term term) {
        return termService.isVersionValid(term);
    }

    protected Boolean isVersionValid(AttachTo attachTo) {
        return termService.isVersionValid(attachTo);
    }

    private String getLatestVersion(UUID uuid) {
        String version;
        List<Term> terms = termService.getTerms(uuid, false);
        if (!terms.isEmpty()) {
            Term head = terms.get(0);
            version = head.getVersion();
        } else {
            version = null;
        }

        return version;
    }

}
