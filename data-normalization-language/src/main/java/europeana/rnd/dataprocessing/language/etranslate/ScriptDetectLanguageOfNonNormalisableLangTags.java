package europeana.rnd.dataprocessing.language.etranslate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.util.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;

import org.apache.tika.exception.TikaException;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;

public class ScriptDetectLanguageOfNonNormalisableLangTags {


	public static void main(String[] args) throws Exception {
		String outputFolder = null;
		String inputFolder = null;

		if (args != null && args.length >= 3) {
			inputFolder = args[0];
			outputFolder = args[1];
		}else {
			inputFolder = "c://users/nfrei/desktop/data/language/not-normalisable";
			outputFolder = "c://users/nfrei/desktop/data/language";
		}

		GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");
		
		LanguageDetector langDetector=LanguageDetector.getDefaultLanguageDetector(); 
		langDetector.loadModels();
		
		try {
			SheetsPrinter sheetsPrinter=new SheetsPrinter("1uWBFwVQS9Z0S-7IYHRAFuipRo64TbKCZxctemHnILis", "tags");
			sheetsPrinter.printRecord("xml:lang value", "detected language", "# of cases", "# detected with high confidence");
			
			for(File codeFile: new File(inputFolder).listFiles()) {
				HashSet<String> alreadyProcessed=new HashSet<String>();
				MapOfInts<String> detectedLanguages=new MapOfInts<String>();
				MapOfInts<String> detectedLanguagesHigh=new MapOfInts<String>();
				
				FileReader fileReader = new FileReader (codeFile);
				CSVParser csvParser=new CSVParser(fileReader, CSVFormat.DEFAULT);
				for(CSVRecord rec: csvParser) {
					String text = rec.get(2);
					if(!alreadyProcessed.contains(text)) {
						alreadyProcessed.add(text);
						LanguageResult detect = langDetector.detect(text);
						detectedLanguages.incrementTo(detect.getLanguage());
						if(detect.isReasonablyCertain())
							detectedLanguagesHigh.incrementTo(detect.getLanguage());
						
//						System.out.println(detect.getLanguage() + " - certain? "+detect.isReasonablyCertain()+ " - conf: "+ detect.getConfidence());
					}					
				}
				System.out.println("---");
				System.out.println(codeFile.getName()+":");

				for( Entry<String, Integer> lang : detectedLanguages.getSortedEntries()) {
					System.out.println(" - "+ lang.getKey()+" - "+lang.getValue() + "("+ (detectedLanguagesHigh.containsKey(lang.getKey()) ? detectedLanguagesHigh.get(lang.getKey()) : 0 ) +")");
					
					sheetsPrinter.printRecord(codeFile.getName().substring(0, codeFile.getName().length()-4) , lang.getKey(), lang.getValue() , (detectedLanguagesHigh.containsKey(lang.getKey()) ? detectedLanguagesHigh.get(lang.getKey()) : 0 ));
				}
				
			}
			sheetsPrinter.close();
		} finally {
			
		}
	}
}
