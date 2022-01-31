package europeana.rnd.dataprocessing.dates;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.FileWriterWithEncoding;

import europeana.rnd.dataprocessing.dates.DatesInRecord.DateValue;
import europeana.rnd.dataprocessing.dates.extraction.Cleaner;
import europeana.rnd.dataprocessing.dates.extraction.DateExtractor;
import europeana.rnd.dataprocessing.dates.extraction.DcmiPeriodExtractor;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;
import europeana.rnd.dataprocessing.dates.extraction.PatternBcAd;
import europeana.rnd.dataprocessing.dates.extraction.PatternCentury;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorYyyyMmDdSpaces;
import europeana.rnd.dataprocessing.dates.extraction.PatternDecade;
import europeana.rnd.dataprocessing.dates.extraction.PatternEdtf;
import europeana.rnd.dataprocessing.dates.extraction.PatternFormatedFullDate;
import europeana.rnd.dataprocessing.dates.extraction.PatternIso8601Date;
import europeana.rnd.dataprocessing.dates.extraction.PatternIso8601DateRange;
import europeana.rnd.dataprocessing.dates.extraction.PatternMonthName;
import europeana.rnd.dataprocessing.dates.extraction.PatternNumericDateExtractorWithMissingParts;
import europeana.rnd.dataprocessing.dates.extraction.PatternNumericDateExtractorWithMissingPartsAndXx;
import europeana.rnd.dataprocessing.dates.extraction.PatternNumericDateRangeExtractorWithMissingParts;
import europeana.rnd.dataprocessing.dates.extraction.PatternNumericDateRangeExtractorWithMissingPartsAndXx;
import europeana.rnd.dataprocessing.dates.extraction.Cleaner.CleanResult;
import europeana.rnd.dataprocessing.dates.extraction.trash.PatternDateExtractorDdMmYyyy;
import europeana.rnd.dataprocessing.dates.extraction.trash.PatternDateExtractorYyyy;
import europeana.rnd.dataprocessing.dates.extraction.trash.PatternDateExtractorYyyyMm;
import europeana.rnd.dataprocessing.dates.extraction.trash.PatternDateRangeExtractorYyyy;
import europeana.rnd.dataprocessing.dates.stats.DateExtractionStatistics;
import europeana.rnd.dataprocessing.dates.stats.HtmlExporter;
import europeana.rnd.dataprocessing.dates.stats.NoMatchSampling;

public class DatesExtractorHandler {
	File outputFolder;
	DateExtractionStatistics stats=new DateExtractionStatistics();
	NoMatchSampling noMatchSampling=new NoMatchSampling();
	static Cleaner cleaner=new Cleaner();
	
	static ArrayList<DateExtractor> extractors=new ArrayList<DateExtractor>() {{
//		add(new PatternDateExtractorYyyy());
		add(new PatternEdtf());
		add(new PatternDateExtractorYyyyMmDdSpaces());
//		add(new PatternDateExtractorDdMmYyyy());
//		add(new PatternDateRangeExtractorYyyy());
		add(new PatternCentury());
		add(new PatternDecade());
		add(new DcmiPeriodExtractor());
		add(new PatternMonthName());
		add(new PatternFormatedFullDate());
//		add(new PatternDateExtractorYyyyMm());
		add(new PatternNumericDateExtractorWithMissingParts());
		add(new PatternNumericDateExtractorWithMissingPartsAndXx());
		add(new PatternNumericDateRangeExtractorWithMissingParts());
		add(new PatternNumericDateRangeExtractorWithMissingPartsAndXx());
//		add(new PatternIso8601Date(false));
//		add(new PatternIso8601DateRange(false));
		add(new PatternBcAd());
	}};
	
	public DatesExtractorHandler(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public DatesExtractorHandler() {
	}
	
	public static void runDateNormalization(DatesInRecord rec) throws Exception {
		for(Match val: rec.getAllValues()) {
			try {
				Match extracted=runDateNormalization(val.getInput()); 
				val.setResult(extracted);
			} catch (Exception e) {
				System.err.println("Error in value: "+val.getInput());
				e.printStackTrace();
				
				val.setResult(new Match(MatchId.NO_MATCH, val.getInput(), null));
			}
		}
	}
	public static Match runDateNormalization(String val) throws Exception {
		String valTrim=val.trim();
		valTrim=valTrim.replace('\u00a0', ' '); //replace non-breaking spaces by normal spaces
		valTrim=valTrim.replace('\u2013', '-'); //replace en dash by normal dash
		
		Match extracted=null; 
		for(DateExtractor extractor: extractors) {
			extracted = extractor.extract(valTrim);
			if (extracted!=null) {
				break;
			}
		}
		if(extracted==null) {
			//Trying patterns after cleaning 
			CleanResult cleanResult = cleaner.clean1st(valTrim);
			if (cleanResult!=null) {
				for(DateExtractor extractor: extractors) {
					extracted = extractor.extract(cleanResult.getCleanedValue());
					if (extracted!=null) {
						extracted.setCleanOperation(cleanResult.getCleanOperation());
						break;
					}
				}										
			}
		}
		if(extracted==null) {
			//Trying patterns after cleaning 
			CleanResult cleanResult = cleaner.clean2nd(valTrim);
			if (cleanResult!=null) {
				for(DateExtractor extractor: extractors) {
					extracted = extractor.extract(cleanResult.getCleanedValue());
					if (extracted!=null) {
						extracted.setCleanOperation(cleanResult.getCleanOperation());
						break;
					}
				}										
			}
		}
		if(extracted==null)
			return new Match(MatchId.NO_MATCH, val, null);
		else
			extracted.setInput(val);
		return extracted;
	}
	
	
	public void handle(DatesInRecord rec) throws Exception {
		runDateNormalization(rec);
		for(DateValue match: rec.getAllValuesDetailed()) {
			handleResult(rec.getChoUri(), match);			
		}
	}
	
	private void handleResult(String choUri, DateValue dateValue) {
		try {
			stats.add(choUri, dateValue);
			Writer wrt=getWriterForMatch(dateValue.match.getMatchId());
			wrt.write(escape(dateValue.match.getInput()));
			if(dateValue.match.getExtracted()!=null) {
				wrt.write(",");
				wrt.write(escape(dateValue.match.getExtracted().serialize()));
			}
			wrt.write("\n");	
			
			if(dateValue.match.getMatchId()==MatchId.NO_MATCH)
				noMatchSampling.add(dateValue.match);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}		
	}

	
	Pattern patternEscape=Pattern.compile("\"");
	private String escape(String val) {
		return "\""+patternEscape.matcher(val).replaceAll("\\\"")+"\"";
	}
	
	
	private HashMap<MatchId, Writer> writers=new HashMap<MatchId, Writer>();
	private Writer getWriterForMatch(MatchId matchID) {
		try {
			Writer wrt = writers.get(matchID);
			if(wrt==null) {
				wrt=new FileWriterWithEncoding(new File(outputFolder, matchID.toString()+".txt"), StandardCharsets.UTF_8);
				writers.put(matchID, wrt);
			}
			return wrt;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public void close() {
		try {
			for(Writer a: writers.values()) 
				a.close();
			stats.save(outputFolder);
			
			HtmlExporter.export(stats, outputFolder);
			
			File outNoMatchSampling=new File(outputFolder, "no-match-samples.csv");
			noMatchSampling.save(outNoMatchSampling);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
