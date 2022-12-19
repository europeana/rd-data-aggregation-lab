package europeana.rnd.dataprocessing.language;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import europeana.rnd.dataprocessing.language.LanguageStatsInDataset.LanguageStatsFromSource;

public class ScriptNormalizeLanguageReport {

	File folder;
	File outputFolder;
	
	LanguageStatsInDataset stats=new LanguageStatsInDataset("Europeana");

	MetisLanguageNormaliser normaliser;
	
	public ScriptNormalizeLanguageReport(File folder, File outputFolder) {
		super();
		this.folder = folder;
		this.outputFolder = outputFolder;
	}

	public void process() throws IOException, NormalizationConfigurationException {
		normaliser=new MetisLanguageNormaliser();

		if(folder.getName().endsWith(".zip")) {
			try (FileInputStream fis = new FileInputStream(folder);
	                BufferedInputStream bis = new BufferedInputStream(fis);
	                ZipInputStream zis = new ZipInputStream(bis)) {
				ZipEntry ze;
	            while ((ze = zis.getNextEntry()) != null) {
	                if(ze.isDirectory()) continue;
					System.out.println(ze.getName());
					processFile(zis);
	            }
			}
		} else {
			for(File innerFolder: folder.listFiles()) {
				if(innerFolder.isFile()) continue;
				if(!innerFolder.getName().startsWith("language_export_")) continue;
				for(File jsonFile: innerFolder.listFiles()) {
					if(jsonFile.isDirectory()) continue;
					System.out.println(jsonFile.getAbsolutePath());
					FileInputStream is = new FileInputStream(jsonFile);
					processFile(is);
					is.close();
				}
			}
		}
		HtmlExporter.export(stats, outputFolder);
	}

	private void processFile(InputStream is) {
		JsonParser parser = Json.createParser(is);
		parser.next();
		Stream<JsonValue> arrayStream = parser.getArrayStream();
		for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
			JsonObject jv=it.next().asJsonObject();
			LanguageInRecord record=new LanguageInRecord(jv);
			try {
				for(LangValue langValue : record.fromEuropeana.getAllLangTagValues()) {
					normalizeLangTag(langValue, stats.fromEuropeana);
				}
				for(PropertyCount propCount : record.fromEuropeana.getAllCountWithoutLangTag()) {
					stats.fromEuropeana.incrementWithoutLangTag(propCount);
				}						
				for(LangValue langValue : record.fromEuropeana.getAllPropertyValues()) {
					normalizeProperty(langValue, stats.fromEuropeana);
				}
				
				for(LangValue langValue : record.fromProvider.getAllLangTagValues()) {
					normalizeLangTag(langValue, stats.fromProvider);
				}
				for(PropertyCount propCount : record.fromProvider.getAllCountWithoutLangTag()) {
					stats.fromProvider.incrementWithoutLangTag(propCount);
				}						
				for(LangValue langValue : record.fromProvider.getAllPropertyValues()) {
					normalizeProperty(langValue, stats.fromProvider);
				}
//						System.out.println(record);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	
	private void normalizeLangTag(LangValue langValue, LanguageStatsFromSource stats) {
		List<LanguageMatch> langMatches = normaliser.normaliseLangTag(langValue.match.getInput());
		if(!langMatches.isEmpty()) {
			for(LanguageMatch lm : langMatches) {
				if(lm.getType()!=Type.NO_MATCH) {
					langValue.match.setNormalized(lm.getMatch());
					break;
				}
			}
		}
		if(langValue.match.getNormalized()!=null) {
			LangSubtagsAnalysis subTags=new LangSubtagsAnalysis(langValue.match.getNormalized());
			if(langValue.match.getNormalized().length()>3 && subTags.subTag==false) {
				System.out.println("normalized value with unusual length: "+langValue.match.getInput()+" -> "+langValue.match.getNormalized());
			}
		}
		stats.addLangTagCase(langValue);
	}
	private void normalizeProperty(LangValue langValue, LanguageStatsFromSource stats) {
		List<LanguageMatch> langMatches = normaliser.normaliseDcLanguage(langValue.match.getInput());
		if(!langMatches.isEmpty()) {
			for(LanguageMatch lm : langMatches) {
				if(lm.getType()!=Type.NO_MATCH) {
					langValue.match.setNormalized(lm.getMatch());
					break;
				}
			}
		}
		stats.addPropertyCase(langValue);
	}

	public static void main(String[] args) throws Exception {
//		String sourceFolder = "c://users/nfrei/desktop/data/language";
//		String sourceFolderStr = "c://users/nfrei/desktop/data/language/language-export.zip";
		String sourceFolderStr = "c://users/nfrei/desktop/data/language/language-export-sep2022.zip";
//		String sourceFolderStr = "c://users/nfrei/desktop/data/language/language-export-small.zip";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		File outFolder=new File(sourceFolderStr.endsWith(".zip") ? sourceFolder.getParentFile() : sourceFolder, "extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptNormalizeLanguageReport processor=new ScriptNormalizeLanguageReport(sourceFolder, outFolder);
		processor.process();
	}
}
