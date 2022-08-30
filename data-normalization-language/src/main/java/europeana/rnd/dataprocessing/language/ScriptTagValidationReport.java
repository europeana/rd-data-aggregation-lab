package europeana.rnd.dataprocessing.language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.settings.NormalizerSettings;
import europeana.rnd.dataprocessing.language.iana.LangTagValidator;
import europeana.rnd.dataprocessing.language.iana.Registry;
import europeana.rnd.dataprocessing.language.iana.Subtag;
import europeana.rnd.dataprocessing.language.iana.ValidationReport;
import europeana.rnd.dataprocessing.language.iana.Subtag.SubtagType;

public class ScriptTagValidationReport {

	
	public static void main(String[] args) throws Exception {
		String sourceFolderStr = "c://users/nfrei/desktop/data/language/extraction";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		Registry reg=new Registry();
		LangTagValidator validator=new LangTagValidator(reg);
		
		for(String filenamePrefix : new String[] {"Language_Europeana_xml_lang_",
		                                       "Language_Provider_xml_lang_"}) {
			for(String filenameSufix : new String[] {
//					"subtags.txt",
					"not_normalizable.txt",
//					"normalizable.txt"
					}) {
				
				int cntValid=0;
				int cntInvalid=0;
				
				String filename=filenamePrefix+filenameSufix;
				System.out.println("Validation of "+filename);
				FileInputStream is=new FileInputStream(new File(sourceFolder, filename));
				BufferedReader reader=new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
				for(String line = reader.readLine() ; line!=null ; line=reader.readLine() ) {
					int idxComa = line.indexOf(',');
					if(idxComa<0)
						continue;
					String tag=line.substring(0, idxComa);
					ValidationReport report = validator.validate(tag);
					System.out.print(tag +" - "+
							(report.isValid() ? "OK" : "Invalid"));
					
					if(report.isValid() && tag.indexOf('-')<0)
						System.out.println(" "+ reg.getSubTag(SubtagType.LANGUAGE, tag).getDescriptions()
								);
					else
						System.out.println(); 
					
					
					if(report.isValid())
						cntValid++;
					else
						cntInvalid++;
				}
				System.out.println("Counts:");
				System.out.println(" - Valid: "+cntValid);
				System.out.println(" - Invalid: "+cntInvalid);
			}
		}
		
		
	}
}
