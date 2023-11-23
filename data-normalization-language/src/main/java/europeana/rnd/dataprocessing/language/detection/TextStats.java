package europeana.rnd.dataprocessing.language.detection;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.google.common.math.Stats;

import europeana.rnd.dataprocessing.language.detection.TextInRecord.TaggedText;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfInts.Sort;
import inescid.util.datastruct.MapOfMapsOfInts;
import inescid.util.language.EuropeanLanguagesNal;
import inescid.util.language.NalLanguage;

public class TextStats {
	private static final int MAX_LENGTH=500;
	private static final int LENGTH_STEP=3;
	
	MapOfInts<Integer> lengthStatsAll=new MapOfInts<Integer>();
	MapOfInts<Integer> lengthStatsOfTagged=new MapOfInts<Integer>();
	MapOfInts<Integer> lengthStatsOfTaggedEuLangTagged=new MapOfInts<Integer>();
	MapOfMapsOfInts<String, Integer> euLangLengthStats=new MapOfMapsOfInts<String, Integer>();

	MapOfInts<Integer> lengthStatsEdmLang=new MapOfInts<Integer>();
	MapOfMapsOfInts<String, Integer> langLengthStatsEdmLang=new MapOfMapsOfInts<String, Integer>();
	
	EuropeanLanguagesNal nal=new EuropeanLanguagesNal();
	
	public void add(String text, String langTag, String edmLang) {
		int length = text.length();
		length=Math.min(MAX_LENGTH, length);
		length=(int)Math.floor(length / LENGTH_STEP)* LENGTH_STEP;
		
//		System.out.println("in:"+langTag+ " " + edmLang);

		if(langTag!=null)
			langTag=nal.normaliseToIsoCode(langTag);
		if(edmLang!=null)
			edmLang=nal.normaliseToIsoCode(edmLang);
				
		lengthStatsAll.incrementTo(length);
		if(langTag!=null) {
			NalLanguage officialEuLanguage = nal.getOfficialEuLanguage(langTag);
			if(officialEuLanguage!=null) {
				
				if(officialEuLanguage.getIso6391().length()!=2) {
					System.out.println("norm:"+langTag+ " " + edmLang);
					System.out.println("official eu:"+officialEuLanguage.getIso6391());
				}
				
				lengthStatsOfTaggedEuLangTagged.incrementTo(length);
				euLangLengthStats.incrementTo(officialEuLanguage.getIso6391(), length);
			} else {
				lengthStatsOfTagged.incrementTo(length);
			}
		} else if(edmLang!=null) {
			NalLanguage officialEuLanguage = nal.getOfficialEuLanguage(edmLang);
			if(officialEuLanguage!=null) {			
				lengthStatsEdmLang.incrementTo(length);
				langLengthStatsEdmLang.incrementTo(officialEuLanguage.getIso6391(), length);
			}
		}
	}
	
	public void add(TextInRecord rec) {
		for(TaggedText ttext: rec.getText()) 
			add(ttext.getText(), ttext.getLang(), rec.getEdmLanguage());
	}
	
	
	public String toCsv() {
		try {
			StringWriter sw=new StringWriter();
			lengthStatsAll.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			sw.append("\n\n");
			lengthStatsOfTagged.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			sw.append("\n\n");
			euLangLengthStats.writeCsv(sw);
			sw.append("\n\n");
			lengthStatsEdmLang.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			sw.append("\n\n");
			langLengthStatsEdmLang.writeCsv(sw);
			return sw.toString();
		} catch (IOException e) { //should not happen
			throw new RuntimeException(e.getMessage(), e);
		}
		
	}

	public void saveCsvs(File outputFolder) throws IOException {
		{
			StringWriter sw=new StringWriter();
			sw.append("Length,Total values (with or without language tag)\n");		
			lengthStatsAll.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			sw.append("\n\n");		
			FileUtils.write(new File(outputFolder,  "text-stats-all.csv"), sw.toString(), StandardCharsets.UTF_8);
		}
		{
			StringWriter sw=new StringWriter();
			sw.append("Length,Total values language tagged\n");		
			lengthStatsOfTagged.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			sw.append("\n\n");		
			FileUtils.write(new File(outputFolder,  "text-stats-lang-tagged-non-eu.csv"), sw.toString(), StandardCharsets.UTF_8);
		}
		{
			StringWriter sw=new StringWriter();
			sw.append("Length,Total values language tagged\n");
			lengthStatsOfTaggedEuLangTagged.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			sw.append("\n\n");		
			FileUtils.write(new File(outputFolder,  "text-stats-lang-tagged-eu.csv"), sw.toString(), StandardCharsets.UTF_8);
		}
		{
			StringWriter sw=new StringWriter();
			sw.append("Length,Total values with edm:language\n");		
			lengthStatsEdmLang.writeCsv(sw, Sort.BY_KEY_ASCENDING);
			sw.append("\n\n");		
			FileUtils.write(new File(outputFolder,  "text-stats-edmLanguage.csv"), sw.toString(), StandardCharsets.UTF_8);
		}
		
		
		
		{
			StringWriter sw=new StringWriter();
			sw.append("Language,Length,Total values\n");	
			euLangLengthStats.writeCsv(sw);
			sw.append("\n\n");		
			FileUtils.write(new File(outputFolder,  "text-stats-lang-tagged-eu-by-lang.csv"), sw.toString(), StandardCharsets.UTF_8);
		}
		
		{
			StringWriter sw=new StringWriter();
			sw.append("Language,Length,Total values\n");	
			langLengthStatsEdmLang.writeCsv(sw);
			sw.append("\n\n");		
			FileUtils.write(new File(outputFolder,  "text-stats-edmLanguage-eu-by-lang.csv"), sw.toString(), StandardCharsets.UTF_8);
		}		
		
		
	}



	
}
