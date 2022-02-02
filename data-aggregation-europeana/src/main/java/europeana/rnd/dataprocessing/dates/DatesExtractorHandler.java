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

import europeana.rnd.dataprocessing.dates.extraction.DateExtractor;
import europeana.rnd.dataprocessing.dates.extraction.DcmiPeriodExtractor;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;
import europeana.rnd.dataprocessing.dates.extraction.PatternCenturyXx;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorDdMmYyyy;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorYyyy;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorYyyyMm;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateExtractorYyyyMmDd;
import europeana.rnd.dataprocessing.dates.extraction.PatternDateRangeExtractorYyyy;
import europeana.rnd.dataprocessing.dates.extraction.PatternFormatedFullDate;
import europeana.rnd.dataprocessing.dates.extraction.PatternMonthName;

public class DatesExtractorHandler {
	File outputFolder;
	Pattern patternSquareBrackets=Pattern.compile("\\[([^\\]]+)\\]");
	
	
	ArrayList<DateExtractor> extractors=new ArrayList<DateExtractor>() {{
		add(new PatternDateExtractorYyyy());
		add(new PatternDateExtractorYyyyMmDd());
		add(new PatternDateExtractorDdMmYyyy());
		add(new PatternDateRangeExtractorYyyy());
		add(new PatternCenturyXx());
		add(new DcmiPeriodExtractor());
		add(new PatternMonthName());
		add(new PatternFormatedFullDate());
		add(new PatternDateExtractorYyyyMm());
	}};
	
	public DatesExtractorHandler(File outputFolder) {
		super();
		this.outputFolder = outputFolder;
	}

	public void handle(DatesInRecord rec) throws Exception {
		for(String val: rec.getAllValues()) {
			Match extracted=null; 
			for(DateExtractor extractor: extractors) {
				extracted = extractor.extract(val);
				if (extracted!=null) {
					handleResult(extracted);
					break;
				}
			}
			if(extracted==null) {
				//Trying patterns after cleaning square brackets
				Matcher m = patternSquareBrackets.matcher(val);
				String newVal=m.replaceAll("$1");
				if(newVal.length()!=val.length()) {
					for(DateExtractor extractor: extractors) {
						extracted = extractor.extract(newVal);
						if (extracted!=null) {
							extracted.setMatchId(MatchId.SquareBrackets);
							extracted.setInput(val);
							handleResult(extracted);
							break;
						}
					}					
				}
			}
			if(extracted==null)
				handleResult(new Match(MatchId.NO_MATCH, val, null));
		}
	}
	
	private void handleResult(Match extracted) {
		try {
			Writer wrt=getWriterForMatch(extracted.getMatchId());
			wrt.write(escape(extracted.getInput()));
			if(extracted.getExtracted()!=null) {
				wrt.write(",");
				wrt.write(escape(extracted.getExtracted()));
			}
			wrt.write("\n");
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
			for(Writer a: writers.values()) {
				a.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
