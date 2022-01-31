package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class PatternDateRangeExtractorYyyyMmDd implements DateExtractor {
	String datePatStr="(\\d\\d\\d\\d)[-/.](\\d\\d?)[-/.](\\d\\d?)";
	String separatorPatStr="(\\s+[-/]\\s+|\\s-\\s)";
	
	Pattern patYyyyRangOpenStart=Pattern.compile("\\s*\\?"+separatorPatStr+datePatStr+"\\s*");
	Pattern patYyyyRangOpenEnd=Pattern.compile("\\s*"+datePatStr+separatorPatStr+"\\?\\s*");
	Pattern patYyyyRang=Pattern.compile("\\s*"+datePatStr+separatorPatStr+datePatStr+"\\s*");
	
	
	public Match extract(String inputValue) {
		Matcher m=patYyyyRang.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.YYYYMMDD_Range, inputValue, m.group(1)+"-"+m.group(2));
		m=patYyyyRangOpenStart.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.YYYYMMDD_Range_Open, inputValue, m.group(1));
		m=patYyyyRangOpenEnd.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.YYYYMMDD_Range_Open, inputValue, m.group(1));
		return null;
	}
	
}
