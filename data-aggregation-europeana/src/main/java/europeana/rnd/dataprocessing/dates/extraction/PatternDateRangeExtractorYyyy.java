package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternDateRangeExtractorYyyy implements DateExtractor {
	Pattern patYyyyRangOpenStart=Pattern.compile("\\s*\\??[-/]\\[?(\\d\\d\\d\\d)\\??\\]?\\s*");
	Pattern patYyyyRangOpenEnd=Pattern.compile("\\s*\\[?(\\d\\d\\d\\d)\\??\\]?[-/]\\??\\s*");
	Pattern patYyyyRang=Pattern.compile("\\s*\\[?(\\d\\d\\d\\d)\\??\\]?\\s?[-/]\\s?\\[?(\\d\\d\\d\\d)\\??\\]?\\s*");	
	Pattern patCa=Pattern.compile("\\s*CA\\.?\\s*(.*)",Pattern.CASE_INSENSITIVE);	
	
	public Match extract(String inputValue) {
		Matcher m=patYyyyRang.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.YYYY_Range, inputValue, m.group(1)+"-"+m.group(2));
		m=patYyyyRangOpenStart.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.YYYY_Range_Open, inputValue, m.group(1));
		m=patYyyyRangOpenEnd.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.YYYY_Range_Open, inputValue, m.group(1));
		m=patCa.matcher(inputValue); 
		if(m.matches()) {
			String newInput=m.group(1);
			m=patYyyyRang.matcher(newInput); 
			if(m.matches())
				return new Match(MatchId.YYYY_Range, inputValue, m.group(1)+"-"+m.group(2));
			m=patYyyyRangOpenStart.matcher(newInput); 
			if(m.matches()) 
				return new Match(MatchId.YYYY_Range_Open, inputValue, m.group(1));
			m=patYyyyRangOpenEnd.matcher(newInput); 
			if(m.matches()) 
				return new Match(MatchId.YYYY_Range_Open, inputValue, m.group(1));
		}
		return null;
	}
	
}
