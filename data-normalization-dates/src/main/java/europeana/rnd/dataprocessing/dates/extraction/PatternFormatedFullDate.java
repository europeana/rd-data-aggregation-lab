package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternFormatedFullDate implements DateExtractor {
	Pattern patFormatedDate=Pattern.compile("\\w{3} (\\w{3}) (\\d{2}) \\d{2}:\\d{2}:\\d{2} \\w{3,4} (\\d{1,4})");
	Pattern patFormatedDate2=Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\w{3,4}\\s?(\\d{0,4})");

	public Match extract(String inputValue) {
		Matcher m=patFormatedDate.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.FormatedFullDate, inputValue, m.group(2)+" "+m.group(1)+" "+m.group(3));
		m=patFormatedDate2.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.FormatedFullDate, inputValue, "");
		return null;
	}
	
}
