package europeana.rnd.dataprocessing.language.detection.evaluation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import apiclient.google.sheets.SheetsPrinter;
import apiclient.google.sheets.SheetsReader;
import europeana.rnd.dataprocessing.language.detection.Result;
import europeana.rnd.dataprocessing.language.detection.Result.DetectedLanguage;
import europeana.rnd.dataprocessing.language.detection.evaluation.TestSet.TextValue;
import inescid.util.StatisticCalcMean;
import inescid.util.datastruct.MapOfMapsOfInts;

public class EvaluationResult {
	
	List<String> languagesSorted; 
	Set<String> languages; 
	
	MapOfMapsOfInts<String, String> results;
	
    public EvaluationResult(Collection<String> evaluatedLanguages) {
    	languagesSorted=new ArrayList<String>(evaluatedLanguages);
    	languages=new HashSet<String>(evaluatedLanguages);
    	Collections.sort(languagesSorted);
    	results=new MapOfMapsOfInts<String, String>();
	}	
    
    public void addResult(TextValue value, Result detected) {
    	DetectedLanguage firstResult = detected.getFirstResult();
		results.incrementTo(value.lang, firstResult==null || !languages.contains(firstResult.getLangCode()) ? "" : firstResult.getLangCode());
    }

	public List<String> getTestedLanguages() {
		return languagesSorted;
	}
	
	
	public void writeToGoogleSheet(String spreadsheetId, String sheetName) throws IOException {
		SheetsPrinter outSheet=new SheetsPrinter(spreadsheetId, sheetName);
		outSheet.printRecord("Language detector: "+sheetName);
		writeToGoogleSheet(outSheet);
		outSheet.close();
	}
	public void writeToGoogleSheet(SheetsPrinter outSheet) throws IOException {
		outSheet.print("");
		for(String lang: languagesSorted)
			outSheet.print(lang);
		outSheet.print("none", "total cases", "", "Correct %", "Accuracy");
		outSheet.println();
		for(String langRow: languagesSorted) {
			outSheet.print(langRow);
			int totalCases=0;
			Integer cellVal;
			for(String langCol: languagesSorted) {
				cellVal = results.get(langRow, langCol);
				if(cellVal==null) cellVal=0;
				outSheet.print(cellVal);
				totalCases+=cellVal;
			}
			cellVal = results.get(langRow, "");
			if(cellVal==null) cellVal=0;
			totalCases+=cellVal;
			outSheet.print(cellVal);
			outSheet.print(totalCases, "", getCorrectProportion(langRow), getAccuracy(langRow));
			outSheet.println();
		}
	}
	

	private float getAccuracy(String lang) {
		float truePositives=0;
		float trueNegatives=0;
		float falsePositives=0;
		float falseNegatives=0;
		for(String langCol: languagesSorted) {
			Integer cnt = results.get(lang, langCol);
			if(cnt==null) cnt=0;
			if(langCol==lang)
				truePositives+=cnt;
			else
				falseNegatives+=cnt;
		}
		for(String langRow: languagesSorted) {
			if(langRow.equals(lang))
				continue;
			for(String langCol: languagesSorted) {
				if(results.containsKey(langRow, langCol)) {
					Integer cnt = results.get(langRow, langCol);
					if(cnt==null) cnt=0;
					if(langCol==lang)
						falsePositives+=cnt;
					else
						trueNegatives+=cnt;
				}
			}
		}		
		return (truePositives + trueNegatives) / (truePositives + trueNegatives + falsePositives + falseNegatives);
	}

	public float getCorrectProportionAverage() {
		StatisticCalcMean calc=new StatisticCalcMean();
		for(String langRow: languagesSorted) {
			calc.enter(getCorrectProportion(langRow));
		}
		return (float)calc.getMean();
	}
	
	private float getCorrectProportion(String lang) {
		float correct=0;
		float incorrect=0;
		for(String langCol: languagesSorted) {
			Integer cnt = results.get(lang, langCol);
			if(cnt==null) cnt=0;
			if(langCol==lang)
				correct+=cnt;
			else
				incorrect+=cnt;
		}
		Integer cnt = results.get(lang, "");
		if(cnt==null) cnt=0;
		incorrect+=cnt;
		return correct / (correct + incorrect);
	}

	public int getSampleSize() {
		int cnt=0;
		for(String lang: languagesSorted) {
			for(String langCol: languagesSorted) 
				cnt += results.get(lang, langCol);
			cnt += results.get(lang, "");
		}
		return cnt;
	}

}
