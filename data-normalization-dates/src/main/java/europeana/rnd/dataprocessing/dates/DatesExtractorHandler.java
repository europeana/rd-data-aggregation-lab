package europeana.rnd.dataprocessing.dates;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;

import europeana.rnd.dataprocessing.dates.DatesInRecord.DateValue;
import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.EdtfValidator;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;
import europeana.rnd.dataprocessing.dates.edtf.TemporalEntity;
import europeana.rnd.dataprocessing.dates.extraction.Cleaner;
import europeana.rnd.dataprocessing.dates.extraction.DateExtractor;
import europeana.rnd.dataprocessing.dates.extraction.DcmiPeriodExtractor;
import europeana.rnd.dataprocessing.dates.extraction.PatternBcAd;
import europeana.rnd.dataprocessing.dates.extraction.PatternBriefDateRange;
import europeana.rnd.dataprocessing.dates.extraction.PatternCentury;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorYyyyMmDdSpaces;
import europeana.rnd.dataprocessing.dates.extraction.PatternDecade;
import europeana.rnd.dataprocessing.dates.extraction.PatternEdtf;
import europeana.rnd.dataprocessing.dates.extraction.PatternFormatedFullDate;
import europeana.rnd.dataprocessing.dates.extraction.PatternLongNegativeYear;
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
import europeana.rnd.dataprocessing.dates.extraction.trash.PatternIso8601Date;
import europeana.rnd.dataprocessing.dates.extraction.trash.PatternIso8601DateRange;
import europeana.rnd.dataprocessing.dates.stats.DateExtractionStatistics;
import europeana.rnd.dataprocessing.dates.stats.HtmlExporter;
import europeana.rnd.dataprocessing.dates.stats.NoMatchSampling;

public class DatesExtractorHandler {
	static DatesNormaliser normaliser=new DatesNormaliser();
	File outputFolder;
	DateExtractionStatistics stats=new DateExtractionStatistics();
	DateExtractionStatistics statsSubjectCoverage=new DateExtractionStatistics();
	NoMatchSampling noMatchSampling=new NoMatchSampling();
	
	public DatesExtractorHandler(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public DatesExtractorHandler() {
	}
	
	public static void runDateNormalization(DatesInRecord rec) throws Exception {
		for(DateValue dateValue: rec.getAllValuesDetailed(Source.ANY)) {
			try {
				Match extracted=null;
				if(dateValue.property.equals("coverage") || dateValue.property.equals("subject")) {
					extracted=normaliser.normaliseGenericProperty(dateValue.match.getInput()); 
				} else {
					extracted=normaliser.normaliseDateProperty(dateValue.match.getInput()); 
				}
				dateValue.match.setResult(extracted);
			} catch (Exception e) {
				System.err.println("Error in value: "+dateValue.match.getInput());
				e.printStackTrace();
				dateValue.match.setResult(new Match(MatchId.NO_MATCH, dateValue.match.getInput(), (TemporalEntity)null));
			}
		}
	}

	public void handle(DatesInRecord rec) throws Exception {
		runDateNormalization(rec);
		for(DateValue match: rec.getAllValuesDetailed(Source.PROVIDER)) 
			handleResult(rec.getChoUri(), Source.PROVIDER, match);			
		for(DateValue match: rec.getAllValuesDetailed(Source.EUROPEANA)) 
			handleResult(rec.getChoUri(), Source.EUROPEANA, match);			
	}
	
	private void handleResult(String choUri, Source source, DateValue dateValue) {
		try {
			if(dateValue.property.equals("coverage") || dateValue.property.equals("subject")) {
				statsSubjectCoverage.add(choUri, source, dateValue);
			} else {
				stats.add(choUri, source, dateValue);
				Writer wrt=getWriterForMatch(dateValue.match.getMatchId());
				wrt.write(escape(dateValue.match.getInput()));
				if(dateValue.match.getExtracted()!=null) {
					wrt.write(",");
					wrt.write(escape(dateValue.match.getExtracted().getEdtf().serialize()));
				}
				wrt.write("\n");	
				if(dateValue.match.getMatchId()==MatchId.NO_MATCH)
					noMatchSampling.add(dateValue.match);
			}			
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
			File covSubToFolder = new File(outputFolder, "coverage-subject");
			if(!covSubToFolder.exists())
				covSubToFolder.mkdir();
			statsSubjectCoverage.save(covSubToFolder);
			
			HtmlExporter.export(stats, statsSubjectCoverage, outputFolder);
			
			File outNoMatchSampling=new File(outputFolder, "no-match-samples.csv");
			noMatchSampling.save(outNoMatchSampling);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
