package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternBcAd implements DateExtractor {
	String patYearBcAd="\\d{2,4}\\s*(BC|AD|AC|DC|VC|NC)";
	Pattern patYyyy=Pattern.compile(patYearBcAd, Pattern.CASE_INSENSITIVE);
	Pattern patRange=Pattern.compile(patYearBcAd+"\\s*[\\-\\/]\\s*"+patYearBcAd, Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m=patYyyy.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.BcAd, inputValue, inputValue);
		m=patRange.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.BcAd, inputValue, inputValue);
		return null;
	}
	
}
