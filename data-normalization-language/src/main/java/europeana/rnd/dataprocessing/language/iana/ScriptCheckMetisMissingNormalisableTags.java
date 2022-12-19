package europeana.rnd.dataprocessing.language.iana;

import java.util.List;

import eu.europeana.normalization.languages.LanguageMatch;
import europeana.rnd.dataprocessing.language.MetisLanguageNormaliser;

public class ScriptCheckMetisMissingNormalisableTags {

	
	public static void main(String[] args) throws Exception {
		MetisLanguageNormaliser normaliser=new MetisLanguageNormaliser();
		Registry reg=new Registry();
		for(Subtag tag: reg.getAllTags(Subtag.SubtagType.LANGUAGE)) {
			List<LanguageMatch> normaliseLangTag = normaliser.normaliseLangTag(tag.getText());
//			System.out.println(tag.getText());
//			System.out.println(normaliseLangTag.get(0).getMatch());
			if(normaliseLangTag.get(0).getMatch()==null) {
				if(tag.getText().length()<=3) {
					System.out.print(tag.getText());
					for(String langName: tag.getDescriptions()) {
						if(langName.contains(","))
							langName=langName.replace(',', ';');
						System.out.print(","+langName);
					}
					System.out.println();
				}
			}else if(! normaliseLangTag.get(0).getMatch().equals(tag.getText()))
				System.out.println("MISMATCH for "+normaliseLangTag.get(0).getMatch()+","+tag.getText()+","+tag.getDescriptions().get(0));
			
		}
		
	}
	
	
}
