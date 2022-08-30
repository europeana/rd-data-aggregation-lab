package europeana.rnd.dataprocessing.language.europeana;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.tika.language.detect.LanguageResult;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import europeana.rnd.dataprocessing.language.europeana.EuropeanaLangTagValueWriter.LangTagValue;
import europeana.rnd.dataprocessing.language.europeana.NotNomalisedTagsReport.ClassReport;
import europeana.rnd.dataprocessing.language.europeana.NotNomalisedTagsReport.PropertyReport;
import europeana.rnd.dataprocessing.language.europeana.NotNomalisedTagsReport.TagValueReport;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.util.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;

public class ScriptCreateReportOfNotNormalisedTags {

	public static void main(String[] args) throws Exception {
		String outputFolder = null;
		String inputFolderPath = null;

		if (args != null && args.length >= 1) {
			inputFolderPath = args[0];
		}else {
			inputFolderPath =  "c://users/nfrei/desktop/data/language/not-normalised-europeana";
		}

		GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");

		NotNomalisedTagsReport report=new NotNomalisedTagsReport();
		
		File inputFolder=new File(inputFolderPath);
		for(File csvFile: inputFolder.listFiles()) {
			if(!csvFile.getName().endsWith(".csv"))
				continue;
		
			String langTag=csvFile.getName().substring(csvFile.getName().indexOf('_')+1, csvFile.getName().lastIndexOf('.'));
			
			CSVParser csvParser=new CSVParser(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8), CSVFormat.DEFAULT);
			for(CSVRecord rec: csvParser) {
				LangTagValue langTagVal = EuropeanaLangTagValueWriter.read(rec);
				langTagVal.value=langTag;
				report.add(langTagVal);
			}
		}
		
		SheetsPrinter sheetsPrinter=new SheetsPrinter("14Tzw6rme7aBlMjjzLNkBbORE7nKlVrZjBN-XvTVofhk", "report");
		sheetsPrinter.printRecord("In class", "In property", "xml:lang value", "# of distinct cases", "# of cases", "Example CHO 1", "Example CHO 2", "Example CHO 3", "Example Entity 1", "Example Entity 2", "Example Entity 3");
		
		for(String rdfType: report.classes.keySet()) {
			ClassReport classReport = report.classes.get(rdfType);
			for(String prop: classReport.properties.keySet()) {
				PropertyReport propReport = classReport.properties.get(prop);
				for(String tag: propReport.tags.keySet()) {
					TagValueReport tagValueReport = propReport.tags.get(tag);

					String[] examplesCho=new String[3];
					int idx=0;
					for(String choUri: tagValueReport.distinctChos) {
						examplesCho[idx]=choUri;
						idx++;
						if(idx>=examplesCho.length)
							break;
					}

					String[] examplesEntity=new String[3];
					idx=0;
					for(String entUri: tagValueReport.distinctEntities) {
						examplesEntity[idx]=entUri;
						idx++;
						if(idx>=examplesEntity.length)
							break;
					}
					
					sheetsPrinter.printRecord(rdfType, prop, tag, tagValueReport.distinctResources.size(),tagValueReport.occurrences
							, examplesCho[0], examplesCho[1], examplesCho[2] 
									, examplesEntity[0], examplesEntity[1], examplesEntity[2] );
				}				
			}
		}
				
		sheetsPrinter.close();
	}
}
