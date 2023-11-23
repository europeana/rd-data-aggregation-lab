package europeana.rnd.dataprocessing.language.detection.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import europeana.rnd.dataprocessing.language.detection.Result;
import europeana.rnd.dataprocessing.language.detection.detector.CachedDetector;
import europeana.rnd.dataprocessing.language.detection.detector.ChatGptApiClient;
import europeana.rnd.dataprocessing.language.detection.detector.Detector;
import europeana.rnd.dataprocessing.language.detection.detector.ETranslationDetector;
import europeana.rnd.dataprocessing.language.detection.detector.GoogleLanguageDetectionApiClient;
import europeana.rnd.dataprocessing.language.detection.detector.PangeanicApiClient;
import europeana.rnd.dataprocessing.language.detection.detector.TikaLanguageDetector;
import europeana.rnd.dataprocessing.language.detection.evaluation.TestSet.TextLength;
import europeana.rnd.dataprocessing.language.detection.evaluation.TestSet.TextValue;
import europeana.rnd.dataprocessing.pid.ScriptProvidersWithPidsReport;
import inescid.util.AccessException;
import inescid.util.StatisticalSignificanceOfTwoPercentages;
import inescid.util.StatisticalSignificanceOfTwoPercentages.PValue;
import inescid.util.datastruct.MapOfMaps;

public class ScriptEvaluateDetectors {
	
	public static void main(String[] args) throws Exception {
		if(new File("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json").exists())
			GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
		else
			GoogleApi.init("/home/nfreire/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
		
		String spreadsheetId = "1QtznDIH7xPneSoevQtvnUUSegWlYriUjNLjL4PlYhhc";
		String testSetPath = "C:\\Users\\nfrei\\Desktop\\data\\language-detection\\language-detection-testset";
		String cachePath = "C:\\Users\\nfrei\\Desktop\\data\\language-detection\\cache";
//		int maxTestCases=0;
//		int maxTestCases=3; // ChatGPT downloaded
		int maxTestCases=250; // google downloaded
//		int maxTestCases=200; // etranslation downloaded
//		int maxTestCases=25; // google downloaded

//		maxTestCases=1000; //as many values as possible 
//		boolean useOnlyCache=true;
		boolean useOnlyCache=false;
		
		if (args != null) {
			if (args.length >= 1) {
				spreadsheetId = args[0];
				if (args.length >= 2) {
					testSetPath = args[1];
					if (args.length >= 3) {
						cachePath = args[2];
					}
				};
			}
		}
		
		File cacheFolder=new File(cachePath);	
		if(!cacheFolder.exists())
			cacheFolder.mkdirs();
			
		CachedDetector tika=new CachedDetector(new TikaLanguageDetector(0), cacheFolder, useOnlyCache);
		CachedDetector pangeanic=new CachedDetector(new PangeanicApiClient(), cacheFolder, useOnlyCache);
		CachedDetector google=new CachedDetector(new GoogleLanguageDetectionApiClient(), cacheFolder, useOnlyCache);
		CachedDetector eTRanslation=new CachedDetector(new ETranslationDetector(), cacheFolder, useOnlyCache);
		CachedDetector chatGpt=new CachedDetector(new ChatGptApiClient(), cacheFolder, useOnlyCache);

		Detector[] detectors=new Detector[] {
				tika
				, pangeanic
				, google
				, eTRanslation
//				, chatGpt
		};
		
		try {
			ScriptEvaluateDetectors processor=new ScriptEvaluateDetectors(new File(testSetPath), maxTestCases, 
					detectors);
			processor.evaluate();
			if(!useOnlyCache) {
				for(Detector det: detectors) {
					if(det instanceof CachedDetector)
						((CachedDetector)det).save();
				}
			}
			processor.writeResultToGoogleSheets(spreadsheetId);
		} catch (AccessException e) {
			System.err.println(e.getCode());
			System.err.println(e.getResponse());
			e.printStackTrace();
		}
	}
		

	Detector[] detectors;
	TestSet testSet;
	int maxValuesPerLanguage;
	Map<Detector, EvaluationResult> resultsLongText;
	Map<Detector, EvaluationResult> resultsShortText;
	Set<String> availableLanguages ;
	
	public ScriptEvaluateDetectors(File testSetFolder, int maxValuesPerLanguage, Detector... detectors) {
		super();
		this.detectors = detectors;
		this.maxValuesPerLanguage = maxValuesPerLanguage;
		testSet=new TestSet(testSetFolder);
		availableLanguages = testSet.getAvailableLanguages();
		resultsLongText=new HashMap<Detector, EvaluationResult>();
		resultsShortText=new HashMap<Detector, EvaluationResult>();
		for(Detector det: detectors) { 
			resultsLongText.put(det, new EvaluationResult(availableLanguages));
			resultsShortText.put(det, new EvaluationResult(availableLanguages));
		}
	}

	public void evaluate() throws IOException, AccessException {
		for(String lang: availableLanguages) {
			for(TextValue testValue: testSet.listValues(lang, TextLength.LONG, maxValuesPerLanguage)) {
				for(Detector det: detectors) {
					Result detected = det.detectLanguage(testValue.text);
					if(detected!=null)
						resultsLongText.get(det).addResult(testValue, detected);
				}
			}
			for(TextValue testValue: testSet.listValues(lang, TextLength.SHORT, maxValuesPerLanguage)) {
				for(Detector det: detectors) {
					Result detected = det.detectLanguage(testValue.text);
					if(detected!=null)
						resultsShortText.get(det).addResult(testValue, detected);
				}
			}
		}		
	}

	public void writeResultToGoogleSheets(String spreadsheetId) throws IOException {
		MapOfMaps<Detector, Detector, PValue> significanceLong=new MapOfMaps<Detector, Detector, PValue>();
		MapOfMaps<Detector, Detector, PValue> significanceShort=new MapOfMaps<Detector, Detector, PValue>();
		
		for(int x=0 ; x<detectors.length ; x++) {
			Detector detectorX=detectors[x];
			for(int y=x+1 ; y<detectors.length ; y++) {
				Detector detectorY=detectors[y];
				{
					EvaluationResult evaluationResultX = resultsLongText.get(detectorX);
					EvaluationResult evaluationResultY = resultsLongText.get(detectorY);
					StatisticalSignificanceOfTwoPercentages statSign=new StatisticalSignificanceOfTwoPercentages();
					statSign.setCategoryA(evaluationResultX.getCorrectProportionAverage(), evaluationResultX.getSampleSize());
					statSign.setCategoryB(evaluationResultY.getCorrectProportionAverage(), evaluationResultY.getSampleSize());
					significanceLong.put(detectorX, detectorY, statSign.calculatePValue());
					
					System.out.println(detectorX.getName()+" "+ evaluationResultX.getCorrectProportionAverage()+" - "+evaluationResultX.getSampleSize());
					System.out.println(detectorY.getName()+" "+evaluationResultY.getCorrectProportionAverage()+" - "+evaluationResultY.getSampleSize());
					System.out.println(statSign.calculatePValue());
					System.out.println("---");
				}
				{
					EvaluationResult evaluationResultX = resultsShortText.get(detectorX);
					EvaluationResult evaluationResultY = resultsShortText.get(detectorY);
					StatisticalSignificanceOfTwoPercentages statSign=new StatisticalSignificanceOfTwoPercentages();
					statSign.setCategoryA(evaluationResultX.getCorrectProportionAverage(), evaluationResultX.getSampleSize());
					statSign.setCategoryB(evaluationResultY.getCorrectProportionAverage(), evaluationResultY.getSampleSize());
					significanceShort.put(detectorX, detectorY, statSign.calculatePValue());
				}				
			}
		}
		
		SheetsPrinter sigSheet=new SheetsPrinter(spreadsheetId, "Significance");
		sigSheet.printRecord("Short text");
		sigSheet.print("");
		for(Detector detector: detectors) {
			sigSheet.print(detector.getName());
		}
		sigSheet.println();
		for(Detector detectorX: detectors) {
			sigSheet.print(detectorX.getName());
			for(Detector detectorY: detectors) {
				if(!significanceShort.containsKey(detectorX, detectorY))
					sigSheet.print("");
				else
					sigSheet.print(significanceShort.get(detectorX, detectorY).toString());
			}
			sigSheet.println();
		}
		sigSheet.println();
		sigSheet.printRecord("Long text");
		sigSheet.print("");
		for(Detector detector: detectors) {
			sigSheet.print(detector.getName());
		}
		sigSheet.println();
		for(Detector detectorX: detectors) {
			sigSheet.print(detectorX.getName());
			for(Detector detectorY: detectors) {
				if(!significanceLong.containsKey(detectorX, detectorY))
					sigSheet.print("");
				else
					sigSheet.print(significanceLong.get(detectorX, detectorY).toString());
			}
			sigSheet.println();
		}
		System.out.println(sigSheet.toCsv());					
		sigSheet.close();			
		
		for(Detector detector: detectors) {
			SheetsPrinter outSheet=new SheetsPrinter(spreadsheetId, detector.getName());
			outSheet.printRecord("Language detector: "+detector.getName());
			outSheet.println();
			outSheet.printRecord("Results on long text (more than 100 characters)");			
			resultsLongText.get(detector).writeToGoogleSheet(outSheet);
			
			outSheet.println();
			outSheet.printRecord("Results on short text (100 characters or less)");			
			resultsShortText.get(detector).writeToGoogleSheet(outSheet);
			
			System.out.println(outSheet.toCsv());			
			
			outSheet.close();			
		}
	}

}
