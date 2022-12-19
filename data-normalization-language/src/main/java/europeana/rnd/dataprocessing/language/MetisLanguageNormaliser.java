package europeana.rnd.dataprocessing.language;

import java.io.File;
import java.io.IOException;
import java.util.List;

import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.settings.NormalizerSettings;
import eu.europeana.normalization.util.NormalizationConfigurationException;

public class MetisLanguageNormaliser {

	LanguageMatcher langTagMatcher;
	LanguageMatcher dcLanguageMatcher; 

	public MetisLanguageNormaliser() throws NormalizationConfigurationException {
		NormalizerSettings settings=new NormalizerSettings();
		langTagMatcher = new LanguageMatcher(
				settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling(),
				settings.getTargetXmlLangVocabularies());
		dcLanguageMatcher = new LanguageMatcher(
				settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling(),
				settings.getTargetDcLanguageVocabularies());
	}

	public List<LanguageMatch> normaliseLangTag(String input) {
		return langTagMatcher.match(input);
	}

	public List<LanguageMatch> normaliseDcLanguage(String input) {
		return dcLanguageMatcher.match(input);
	}
}
