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
import org.apache.commons.lang3.StringUtils;

import europeana.rnd.dataprocessing.dates.DatesInRecord.DateValue;
import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.EdtfValidator;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;
import europeana.rnd.dataprocessing.dates.edtf.TemporalEntity;
import europeana.rnd.dataprocessing.dates.extraction.CleanId;
import europeana.rnd.dataprocessing.dates.extraction.Cleaner;
import europeana.rnd.dataprocessing.dates.extraction.DateExtractor;
import europeana.rnd.dataprocessing.dates.extraction.DcmiPeriodExtractor;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;
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
	File outputFolder;
	DateExtractionStatistics stats=new DateExtractionStatistics();
	DateExtractionStatistics statsSubjectCoverage=new DateExtractionStatistics();
	NoMatchSampling noMatchSampling=new NoMatchSampling();
	static Cleaner cleaner=new Cleaner();
	
	static ArrayList<DateExtractor> extractors=new ArrayList<DateExtractor>() {{
//		add(new PatternDateExtractorYyyy());
		add(new PatternBriefDateRange());
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
		add(new PatternLongNegativeYear());
	}};
	static ArrayList<Class> extractorsExcludedForGenericProperties=new ArrayList<Class>() {{
		add(PatternBriefDateRange.class);
	}};
//	static ArrayList<DateExtractor> extractorsGenericProperties=new ArrayList<DateExtractor>() {{
//		add(new PatternEdtf());
//		add(new PatternDateExtractorYyyyMmDdSpaces());
//		add(new DcmiPeriodExtractor());
//		add(new PatternMonthName());
//		add(new PatternFormatedFullDate());
//		add(new PatternNumericDateExtractorWithMissingParts());
//		add(new PatternNumericDateExtractorWithMissingPartsAndXx());
//		add(new PatternNumericDateRangeExtractorWithMissingParts());
//		add(new PatternNumericDateRangeExtractorWithMissingPartsAndXx());
//		add(new PatternLongNegativeYear());
//	}};
	
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
					extracted=runDateNormalizationOnGenericProperty(dateValue.match.getInput()); 
					if(extracted.getMatchId()!=MatchId.NO_MATCH) {
						if(!EdtfValidator.validate(extracted.getExtracted(), false)) 
							extracted.setMatchId(MatchId.INVALID);
					}
				} else {
					extracted=runDateNormalization(dateValue.match.getInput(), true); 
					if(extracted.getMatchId()!=MatchId.NO_MATCH) {
						if(!EdtfValidator.validate(extracted.getExtracted(), false)) {
							if(extracted.getExtracted() instanceof Interval) {
								//lets try to invert the start and end dates and see if it validates
								Interval i=(Interval)extracted.getExtracted();
								Instant start = i.getStart();
								i.setStart(i.getEnd());
								i.setEnd(start);
								if(!EdtfValidator.validate(extracted.getExtracted(), false)) {
									i.setEnd(i.getStart());
									i.setStart(start);
									extracted.setMatchId(MatchId.INVALID);
								}
							} else
								extracted.setMatchId(MatchId.INVALID);
						}
						if(extracted.getMatchId()!=MatchId.NO_MATCH && extracted.getMatchId()!=MatchId.INVALID) {
							if(extracted.getExtracted().isTimeOnly())
								extracted.setMatchId(MatchId.NO_MATCH);
						}
					}
				}
				dateValue.match.setResult(extracted);
			} catch (Exception e) {
				System.err.println("Error in value: "+dateValue.match.getInput());
				e.printStackTrace();
				dateValue.match.setResult(new Match(MatchId.NO_MATCH, dateValue.match.getInput(), null));
			}
		}
	}
	public static Match runDateNormalization(String val, boolean validateAndFix) throws Exception {
		String valTrim=val.trim();
		valTrim=valTrim.replace('\u00a0', ' '); //replace non-breaking spaces by normal spaces
		valTrim=valTrim.replace('\u2013', '-'); //replace en dash by normal dash
		
		Match extracted=null;
		for(DateExtractor extractor: extractors) {
			extracted = extractor.extract(valTrim);
			if (extracted!=null) 
				break;
		}
		if(extracted==null) {
			//Trying patterns after cleaning 
			CleanResult cleanResult = cleaner.clean1st(valTrim);
			if (cleanResult!=null && !StringUtils.isEmpty(cleanResult.getCleanedValue())) {
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
			if (cleanResult!=null && !StringUtils.isEmpty(cleanResult.getCleanedValue())) {
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
		
		if(extracted.getCleanOperation()!=null) {
			if(extracted.getCleanOperation()==CleanId.CIRCA) {
				extracted.getExtracted().setApproximate(true);
			} else if(extracted.getCleanOperation()==CleanId.SQUARE_BRACKETS) {
				extracted.getExtracted().setUncertain(true);
			} else if(extracted.getCleanOperation()==CleanId.SQUARE_BRACKETS_AND_CIRCA) {
				extracted.getExtracted().setUncertain(true);
				extracted.getExtracted().setApproximate(true);
			} else if(extracted.getCleanOperation()==CleanId.PARENTHESES_FULL_VALUE_AND_CIRCA) {
				extracted.getExtracted().setUncertain(true);
				extracted.getExtracted().setApproximate(true);
			} else if(extracted.getCleanOperation()==CleanId.PARENTHESES_FULL_VALUE) {
				extracted.getExtracted().setUncertain(true);
			}
		}
		if(validateAndFix)
			validateAndFix(extracted);

		if(extracted.getMatchId()==MatchId.Edtf && extracted.getCleanOperation()!=null) 
			extracted.setMatchId(MatchId.Edtf_Cleaned);
		
		return extracted;
	}
	public static Match runDateNormalizationOnGenericProperty(String val) throws Exception {
		String valTrim=val.trim();
		valTrim=valTrim.replace('\u00a0', ' '); //replace non-breaking spaces by normal spaces
		valTrim=valTrim.replace('\u2013', '-'); //replace en dash by normal dash
		
		Match extracted=null;
		for(DateExtractor extractor: extractors) {
			if(extractorsExcludedForGenericProperties.contains(extractor.getClass()))
				continue;
			extracted = extractor.extract(valTrim);
			if (extracted!=null) 
				break;
		}
		if(extracted==null) {
			//Trying patterns after cleaning 
			CleanResult cleanResult = cleaner.cleanGenericProperty(valTrim);
			if (cleanResult!=null && !StringUtils.isEmpty(cleanResult.getCleanedValue())) {
				for(DateExtractor extractor: extractors) {
					if(extractorsExcludedForGenericProperties.contains(extractor.getClass()))
						continue;
					extracted = extractor.extract(cleanResult.getCleanedValue());
					if (extracted!=null) {
						extracted.setCleanOperation(cleanResult.getCleanOperation());
						break;
					}
				}										
			}
		}
		if(extracted==null || !extracted.isCompleteDate())
			return new Match(MatchId.NO_MATCH, val, null);
		else 
			extracted.setInput(val);
		
		if(extracted.getCleanOperation()!=null) {
			if(extracted.getCleanOperation()==CleanId.CIRCA) {
				extracted.getExtracted().setApproximate(true);
			} else if(extracted.getCleanOperation()==CleanId.SQUARE_BRACKETS) {
				extracted.getExtracted().setUncertain(true);
			} else if(extracted.getCleanOperation()==CleanId.SQUARE_BRACKETS_AND_CIRCA) {
				extracted.getExtracted().setUncertain(true);
				extracted.getExtracted().setApproximate(true);
			}
		}
		
		if(extracted.getMatchId()==MatchId.Edtf && extracted.getCleanOperation()!=null) {
			extracted.setMatchId(MatchId.Edtf_Cleaned);
		}
		
		return extracted;
	}

	private static void validateAndFix(Match extracted) {
		boolean trySwitchDayMonth=true;
		if(extracted.getMatchId()!=MatchId.NO_MATCH) {
			if(!EdtfValidator.validate(extracted.getExtracted(), false)) {
				if(extracted.getExtracted() instanceof Interval) {
					//lets try to invert the start and end dates and see if it validates
					Interval i=(Interval)extracted.getExtracted();
					Instant start = i.getStart();
					i.setStart(i.getEnd());
					i.setEnd(start);
					if(!EdtfValidator.validate(extracted.getExtracted(), false)) {
						i.setEnd(i.getStart());
						i.setStart(start);
						
						if(trySwitchDayMonth) {
							TemporalEntity copy = extracted.getExtracted().copy();
							copy.switchDayMonth();
							if(!EdtfValidator.validate(copy, false)) {
								extracted.setMatchId(MatchId.INVALID);
							} else 
								extracted.setExtracted(copy);
						}else
							extracted.setMatchId(MatchId.INVALID);
					}
				} else {
					if(trySwitchDayMonth) {
						TemporalEntity copy = extracted.getExtracted().copy();
						copy.switchDayMonth();
						if(!EdtfValidator.validate(copy, false)) {
							extracted.setMatchId(MatchId.INVALID);
						} else 
							extracted.setExtracted(copy);
					}else
						extracted.setMatchId(MatchId.INVALID);
				}
			}
			if(extracted.getMatchId()!=MatchId.NO_MATCH && extracted.getMatchId()!=MatchId.INVALID) {
				if(extracted.getExtracted().isTimeOnly())
					extracted.setMatchId(MatchId.NO_MATCH);
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
					wrt.write(escape(dateValue.match.getExtracted().serialize()));
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
