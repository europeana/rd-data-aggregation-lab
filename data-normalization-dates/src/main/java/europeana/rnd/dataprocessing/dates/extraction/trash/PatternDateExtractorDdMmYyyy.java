package europeana.rnd.dataprocessing.dates.extraction.trash;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.Match;
import europeana.rnd.dataprocessing.dates.MatchId;
import europeana.rnd.dataprocessing.dates.extraction.DateExtractor;

public class PatternDateExtractorDdMmYyyy{}
//public class PatternDateExtractorDdMmYyyy implements DateExtractor {
//	Pattern patYyyyMmDd=Pattern.compile("\\s*(\\d\\d?)[-/. ](\\d\\d?)[-/. ](\\d\\d\\d\\d)\\s*");
//	Pattern patYyyyMmDdX=Pattern.compile("\\s*([\\[\\?]{0,2})(\\d\\d?)[-/. ](\\d\\d?)[-/. ](\\d\\d\\d\\d)[\\]\\?]{0,2}\\s*");
//	
//	public Match extract(String inputValue) {
//		Matcher m=patYyyyMmDd.matcher(inputValue); 
//		if(m.matches())
//			return new Match(MatchId.DD_MM_YYYY, inputValue, m.group(1)+"-"+m.group(2)+"-"+m.group(3));
//		m=patYyyyMmDdX.matcher(inputValue); 
//		if(m.matches()) 
//			return new Match(MatchId.xDD_MM_YYYYx, inputValue, m.group(2)+"-"+m.group(3)+"-"+m.group(4));
//		return null;
//	}
//	
//}
