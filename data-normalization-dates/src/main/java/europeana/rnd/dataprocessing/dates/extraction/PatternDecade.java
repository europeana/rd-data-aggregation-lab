package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternDecade implements DateExtractor {
	Pattern patYyyy=Pattern.compile("\\s*\\??(\\d\\d\\d)[x\\-\\?]\\??\\s*",Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m=patYyyy.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.Decade, inputValue, inputValue);
		return null;
	}
	
}
