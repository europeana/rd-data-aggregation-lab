package europeana.rnd.dataprocessing.language;

import java.util.List;

import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.settings.NormalizerSettings;

public class ScriptTest {

	
	public static void main(String[] args) throws Exception {
		NormalizerSettings settings=new NormalizerSettings();
		LanguageMatcher langTagMatcher = new LanguageMatcher(
				settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling(),
				settings.getTargetXmlLangVocabularies());
		LanguageMatcher dcLanguageMatcher = new LanguageMatcher(
				settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling(),
				settings.getTargetDcLanguageVocabularies());

//		List<LanguageMatch> langMatches = langTagMatcher.match("EN");
//		List<LanguageMatch> langMatches = langTagMatcher.match("DE");
		List<LanguageMatch> langMatches = dcLanguageMatcher.match("EN");
//		List<LanguageMatch> langMatches = dcLanguageMatcher.match("DE");
		for(LanguageMatch lm: langMatches) {
			System.out.println(lm.getMatch());
			
		}
		
	}
}
