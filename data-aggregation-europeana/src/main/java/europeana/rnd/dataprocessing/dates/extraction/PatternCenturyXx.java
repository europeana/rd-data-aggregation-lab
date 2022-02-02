package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternCenturyXx implements DateExtractor {
	Pattern patYyyy=Pattern.compile("\\s*(\\d\\d)xx\\s*",Pattern.CASE_INSENSITIVE);
	Pattern patYyyyX=Pattern.compile("\\s*([\\[\\?]{0,2})(\\d\\d)xx[\\]\\?]{0,2}\\s*",Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m=patYyyy.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.Century, inputValue, m.group(1)+"XX");
		m=patYyyyX.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.CenturyX, inputValue, m.group(2)+"XX");
		return null;
	}
	
}
