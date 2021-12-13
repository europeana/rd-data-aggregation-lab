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

import europeana.rnd.dataprocessing.dates.extraction.Cleaner;
import europeana.rnd.dataprocessing.dates.extraction.DateExtractor;
import europeana.rnd.dataprocessing.dates.extraction.DcmiPeriodExtractor;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;
import europeana.rnd.dataprocessing.dates.extraction.PatternBcAd;
import europeana.rnd.dataprocessing.dates.extraction.PatternCentury;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorDdMmYyyy;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorYyyy;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorYyyyMm;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorYyyyMmDdSpaces;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateRangeExtractorYyyy;
import europeana.rnd.dataprocessing.dates.extraction.PatternDecade;
import europeana.rnd.dataprocessing.dates.extraction.PatternFormatedFullDate;
import europeana.rnd.dataprocessing.dates.extraction.PatternIso8601Date;
import europeana.rnd.dataprocessing.dates.extraction.PatternIso8601DateRange;
import europeana.rnd.dataprocessing.dates.extraction.PatternMonthName;
import europeana.rnd.dataprocessing.dates.extraction.PatternNumericDateExtractorWithMissingParts;
import europeana.rnd.dataprocessing.dates.extraction.PatternNumericDateExtractorWithMissingPartsAndXx;
import europeana.rnd.dataprocessing.dates.extraction.PatternNumericDateRangeExtractorWithMissingParts;
import europeana.rnd.dataprocessing.dates.extraction.PatternNumericDateRangeExtractorWithMissingPartsAndXx;
import europeana.rnd.dataprocessing.dates.extraction.Cleaner.CleanResult;

public class DatesExtractorHandlerViability {
	File outputFolder;
	DateExtractionStatistics stats=new DateExtractionStatistics();
	NoMatchSampling noMatchSampling=new NoMatchSampling();
	static Cleaner cleaner=new Cleaner();
	
	static ArrayList<DateExtractor> extractors=new ArrayList<DateExtractor>() {{
//		add(new PatternDateExtractorYyyy());
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
		add(new PatternIso8601Date());
		add(new PatternIso8601DateRange());
		add(new PatternBcAd());
	}};
	
	public DatesExtractorHandlerViability(File outputFolder) {
		super();
		this.outputFolder = outputFolder;
	}
	
	public static void runDateNormalization(DatesInRecord rec) throws Exception {
		for(Match val: rec.getAllValues()) {
			Match extracted=runDateNormalization(val.getInput()); 
			val.setResult(extracted);
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
		for(Match match: rec.getAllValues()) {
			handleResult(rec.getChoUri(), match);			
		}
	}
	
	private void handleResult(String choUri, Match extracted) {
		try {
			stats.add(choUri, extracted);
			Writer wrt=getWriterForMatch(extracted.getMatchId());
			wrt.write(escape(extracted.getInput()));
			if(extracted.getExtracted()!=null) {
				wrt.write(",");
				wrt.write(escape(extracted.getExtracted()));
			}
			wrt.write("\n");	
			
			if(extracted.getMatchId()==MatchId.NO_MATCH)
				noMatchSampling.add(extracted);
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
			File outGlobalStats=new File(outputFolder, "stats-global.csv");
			File outGlobalCleanStats=new File(outputFolder, "stats-global-clean.csv");
			File outColStats=new File(outputFolder, "stats-collections.csv");
			stats.save(outGlobalStats, outGlobalCleanStats, outColStats);
			
			File outNoMatchSampling=new File(outputFolder, "no-match-samples.csv");
			noMatchSampling.save(outNoMatchSampling);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
