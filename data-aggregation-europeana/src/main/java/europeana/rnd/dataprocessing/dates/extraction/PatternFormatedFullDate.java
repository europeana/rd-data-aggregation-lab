package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternFormatedFullDate implements DateExtractor {
	Pattern patFormatedDAte=Pattern.compile("\\w{3} (\\w{3}) (\\d\\d) \\d\\d:\\d\\d:\\d\\d \\w{3} (\\d{4})");

	public Match extract(String inputValue) {
		Matcher m=patFormatedDAte.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.FormatedFullDate, inputValue, m.group(2)+" "+m.group(1)+" "+m.group(3));
		return null;
	}
	
}
