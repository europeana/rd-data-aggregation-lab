package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternIso8601DateRange extends PatternIso8601Date implements DateExtractor {
	Pattern patIsoDateRangeBrief=Pattern.compile(patIsoDateBriefString+"/"+patIsoDateBriefString);
	
	Pattern patIsoDateRange=Pattern.compile(patIsoDateString+"/"+patIsoDateString);

	
	public Match extract(String inputValue) {
		Matcher m=patIsoDateRange.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.Iso8601DateRange, inputValue, m.group(2)+" "+m.group(1)+" "+m.group(3));
		 m=patIsoDateRangeBrief.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.Iso8601BriefDateRange, inputValue, m.group(2)+" "+m.group(1)+" "+m.group(3));
		return null;
	}
	
	
	public static void main(String[] args) throws Exception {
		PatternIso8601DateRange pat=new PatternIso8601DateRange();
		System.out.println(pat.extract("2021-11-09"));
		System.out.println(pat.extract("2021-11-09T09:44"));
		System.out.println(pat.extract("2021-11-09T09:44:12+00:00"));
		System.out.println(pat.extract("2021-11-09T09:44:12.123+00:00"));
		System.out.println(pat.extract("2021-11-09T09:44:12Z"));
		System.out.println(pat.extract("20211109T094412Z"));
		System.out.println(pat.extract("20211109T0944"));
		System.out.println(pat.extract("20211109"));
		System.out.println(pat.extract("-2021"));
	}
}
