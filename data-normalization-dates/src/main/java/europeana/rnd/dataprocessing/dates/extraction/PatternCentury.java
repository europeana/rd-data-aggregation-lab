package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternCentury implements DateExtractor {
	Pattern patYyyy=Pattern.compile("\\s*\\??(\\d{2})\\.{2}\\??\\s*",Pattern.CASE_INSENSITIVE);
	Pattern patRoman=Pattern.compile("\\s*(s\\.?|sec\\.?)\\s*[XIV]{1,5}\\s*",Pattern.CASE_INSENSITIVE);
//	Pattern patYyyyX=Pattern.compile("\\s*([\\[\\?]{0,2})(\\d\\d)xx[\\]\\?]{0,2}\\s*",Pattern.CASE_INSENSITIVE);
	Pattern patRomanClean=Pattern.compile("\\s*(I{1,3}|IV|VI{0,3}|I?X|XI{1,3}|XIV|XVI{0,3}|I?XX|XXI)\\s*",Pattern.CASE_INSENSITIVE);

	Pattern patRomanRange=Pattern.compile("\\s*(s\\.?|sec\\.?)\\s*[XIV]{1,5}\\s*"+"\\-"+
			"\\s*[XIV]{1,5}\\s*"
			,Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m; 
		m=patYyyy.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.Century_Numeric, inputValue, inputValue);
		m=patRoman.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.Century_Roman, inputValue, inputValue);
		m=patRomanClean.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.Century_Roman, inputValue, inputValue);
		m=patRomanRange.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.Century_Range_Roman, inputValue, inputValue);
		return null;
	}
	
}
