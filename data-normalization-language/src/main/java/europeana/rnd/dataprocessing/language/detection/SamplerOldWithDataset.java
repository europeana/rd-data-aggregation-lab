package europeana.rnd.dataprocessing.language.detection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import inescid.util.datastruct.BigCollection;
import inescid.util.datastruct.BigMap;
import inescid.util.language.EuropeanLanguagesNal;
import inescid.util.language.NalLanguage;

public class SamplerOldWithDataset {
	
	class SamplerOfLanguage {
		String langCode;
	//	HashSet<String> datasetsContaining=new HashSet<String>();
	
		//Map of 'value'->'set of collections where value exists'
		BigMap<String, HashSet<String>> distinctValuesAndCollectionsIn;
		
		public SamplerOfLanguage(BigCollection db, String langCode) {
			super();
			this.langCode = langCode;
			distinctValuesAndCollectionsIn=new BigMap<String, HashSet<String>>(langCode, db);
		}
		
		public void addValue(String value, String dataset) {
			HashSet<String> datasets = distinctValuesAndCollectionsIn.get(value);
			if(datasets!=null) {
				if(!datasets.contains(dataset)) {
					datasets.add(dataset);
					distinctValuesAndCollectionsIn.put(value, datasets);
				}
			} else {
				HashSet<String> dss=new HashSet<String>();
				dss.add(dataset);
				distinctValuesAndCollectionsIn.put(value, dss);
			}
		}

		public int[] exportSample(File exportFolder, int maxValues) throws IOException {
			int countExportedShort=0;
			int countExportedLong=0;
			if(!distinctValuesAndCollectionsIn.isEmpty()) {
				File outFileShort=new File(exportFolder, langCode+"-short.txt");
				File outFileLong=new File(exportFolder, langCode+"-long.txt");
				FileWriter fwShort=new FileWriter(outFileShort, StandardCharsets.UTF_8);
				FileWriter fwLong=new FileWriter(outFileLong, StandardCharsets.UTF_8);
				CSVPrinter csvShort=new CSVPrinter(fwShort, CSVFormat.DEFAULT);
				CSVPrinter csvLong=new CSVPrinter(fwLong, CSVFormat.DEFAULT);
				for(String txt:distinctValuesAndCollectionsIn.getMap().keySet()) {				
					if(txt.length()<=100) {
						if(countExportedShort<maxValues) {
							csvShort.printRecord(txt);
							countExportedShort++;
						}
					} else {
						if(countExportedLong<maxValues) {
							csvLong.printRecord(txt);
							countExportedLong++;
						}
					}
					if(countExportedShort>=maxValues && countExportedLong>=maxValues)
						break;
				}
				csvShort.close();
				fwShort.close();
				csvLong.close();
				fwLong.close();
			}
			return new int[] {countExportedShort, countExportedLong};
		}

	}	
	
	//Map of 'language 2 letter code' -> sampler
	BigCollection db;
	HashMap<String,	SamplerOfLanguage> samplers;
	EuropeanLanguagesNal nal;
	
	public SamplerOldWithDataset(File dbFolder, EuropeanLanguagesNal nal) {
		db=new BigCollection(dbFolder, true);
		samplers=new HashMap<String, SamplerOldWithDataset.SamplerOfLanguage>(24);
		this.nal=nal;
		for(NalLanguage lang: nal.getOfficialEuLanguages()) {
			samplers.put(lang.getIso6391(), new SamplerOfLanguage(db, lang.getIso6391()));
		}
	}
	
	public void processValue(String text, String lang, String dataset) {
		if(StringUtils.isEmpty(lang))
			return;
		String langNormal=nal.normaliseToIsoCode(lang);
		SamplerOfLanguage samplerOfLanguage = samplers.get(langNormal);
		if(samplerOfLanguage!=null) {
			samplerOfLanguage.addValue(text, dataset);
		}
	}

	public void close() {
		db.close();
	}

	public void exportSample(File exportFolder, int maxValues) throws IOException {
		for(Entry<String, SamplerOfLanguage> entry: samplers.entrySet()) {
			int[] valuesExported=entry.getValue().exportSample(exportFolder, maxValues);
			System.out.println(entry.getKey()+" - "+valuesExported[0]+ " small values / "+valuesExported[1]+ " long values");
		}
	}
	
	
	
}
