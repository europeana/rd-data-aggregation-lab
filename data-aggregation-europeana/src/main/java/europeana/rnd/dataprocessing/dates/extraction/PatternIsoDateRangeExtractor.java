package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternIsoDateRangeExtractor implements DateExtractor {
//	Pattern patYyyyRangOpenStart=Pattern.compile("\\s*\\??-(\\d\\d\\d\\d)\\s*");
	
	
	
	Pattern patYyyyRangOpenStart=Pattern.compile("\\s*\\??-(\\d\\d\\d\\d)\\s*");
	Pattern patYyyyRangOpenEnd=Pattern.compile("\\s*(\\d\\d\\d\\d)-\\??\\s*");
	Pattern patYyyyRang=Pattern.compile("\\s*(\\d\\d\\d\\d)-(\\d\\d\\d\\d)\\??\\s*");	
	
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
		return null;
	}
	
}
