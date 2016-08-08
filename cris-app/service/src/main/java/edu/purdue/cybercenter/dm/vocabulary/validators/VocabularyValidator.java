package edu.purdue.cybercenter.dm.vocabulary.validators;

import edu.purdue.cybercenter.dm.xml.vocabulary.Term;

public interface VocabularyValidator {

    public Object validate(Term term, Object value);

}
