package europeana.rnd.dataprocessing.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import eu.europeana.normalization.Normalizer;
import eu.europeana.normalization.NormalizerFactory;
import eu.europeana.normalization.NormalizerStep;
import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.normalizers.ValueNormalizeAction;
import eu.europeana.normalization.normalizers.XmlLangNormalizer;
import eu.europeana.normalization.settings.NormalizerSettings;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import europeana.rnd.dataprocessing.language.LanguageInRecord.LangValue;
import europeana.rnd.dataprocessing.language.LanguageInRecord.PropertyCount;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LangStatsOfItem;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsFromSource;
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsInClass;

public class ScriptNormalizeLanguageReport {

	File folder;
	File outputFolder;
	
	public ScriptNormalizeLanguageReport(File folder, File outputFolder) {
		super();
		this.folder = folder;
		this.outputFolder = outputFolder;
	}

	public void process() throws IOException, NormalizationConfigurationException {
		NormalizerSettings settings=new NormalizerSettings();
		LanguageMatcher langTagMatcher = new LanguageMatcher(
				settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling(),
				settings.getTargetXmlLangVocabularies());
		LanguageMatcher dcLanguageMatcher = new LanguageMatcher(
				settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling(),
				settings.getTargetDcLanguageVocabularies());

		LanguageStatsInDataset stats=new LanguageStatsInDataset("Europeana");
		for(File innerFolder: folder.listFiles()) {
			if(innerFolder.isFile()) continue;
			if(!innerFolder.getName().startsWith("language_export_")) continue;
			for(File jsonFile: innerFolder.listFiles()) {
				if(jsonFile.isDirectory()) continue;
				System.out.println(jsonFile.getAbsolutePath());
				FileInputStream is = new FileInputStream(jsonFile);
				JsonParser parser = Json.createParser(is);
				parser.next();
				Stream<JsonValue> arrayStream = parser.getArrayStream();
				for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
					JsonObject jv=it.next().asJsonObject();
					LanguageInRecord record=new LanguageInRecord(jv);
					try {
						for(LangValue langValue : record.fromEuropeana.getAllLangTagValues()) {
							normalizeLangTag(langValue, langTagMatcher, stats.fromEuropeana);
						}
						for(PropertyCount propCount : record.fromEuropeana.getAllCountWithoutLangTag()) {
							stats.fromEuropeana.incrementWithoutLangTag(propCount);
						}						
						for(LangValue langValue : record.fromEuropeana.getAllPropertyValues()) {
							normalizeProperty(langValue, dcLanguageMatcher, stats.fromEuropeana);
						}
						
						for(LangValue langValue : record.fromProvider.getAllLangTagValues()) {
							normalizeLangTag(langValue, langTagMatcher, stats.fromProvider);
						}
						for(PropertyCount propCount : record.fromProvider.getAllCountWithoutLangTag()) {
							stats.fromProvider.incrementWithoutLangTag(propCount);
						}						
						for(LangValue langValue : record.fromProvider.getAllPropertyValues()) {
							normalizeProperty(langValue, dcLanguageMatcher, stats.fromProvider);
						}
//						System.out.println(record);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				is.close();
			}
		}
		HtmlExporter.export(stats, outputFolder);
	}


	private void normalizeLangTag(LangValue langValue, LanguageMatcher langTagMatcher, LanguageStatsFromSource stats) {
		List<LanguageMatch> langMatches = langTagMatcher.match(langValue.match.getInput());
		if(!langMatches.isEmpty()) {
			for(LanguageMatch lm : langMatches) {
				if(lm.getType()!=Type.NO_MATCH) {
					langValue.match.setNormalized(lm.getMatch());
					break;
				}
			}
		}
		LangSubtagsAnalysis subTags=null;
		if(langValue.match.getNormalized()!=null) {
			subTags=new LangSubtagsAnalysis(langValue.match.getNormalized());
			if(langValue.match.getNormalized().length()!=2) {
				System.out.println("normalized value with unusual length: "+langValue.match.getInput()+" -> "+langValue.match.getNormalized());
			}
		}
		stats.addLangTagCase(langValue, subTags);
	}
	private void normalizeProperty(LangValue langValue, LanguageMatcher langMatcher, LanguageStatsFromSource stats) {
		List<LanguageMatch> langMatches = langMatcher.match(langValue.match.getInput());
		if(!langMatches.isEmpty()) {
			for(LanguageMatch lm : langMatches) {
				if(lm.getType()!=Type.NO_MATCH) {
					langValue.match.setNormalized(lm.getMatch());
					break;
				}
			}
		}
		LangSubtagsAnalysis subTags=null;
		if(langValue.match.getNormalized()!=null) 
			subTags=new LangSubtagsAnalysis(langValue.match.getNormalized());
		stats.addPropertyCase(langValue, subTags);
	}

	public static void main(String[] args) throws Exception {
		String sourceFolder = "c://users/nfrei/desktop/data/language";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolder = args[0];
			}
		}
		File outFolder=new File(sourceFolder+"/extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptNormalizeLanguageReport processor=new ScriptNormalizeLanguageReport(new File(sourceFolder), outFolder);
		processor.process();
	}
}
