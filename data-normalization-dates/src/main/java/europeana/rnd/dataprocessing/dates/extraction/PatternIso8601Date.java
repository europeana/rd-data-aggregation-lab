package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternIso8601Date implements DateExtractor {
	String patIsoDateBriefString="-?(\\d{4})(\\d{2})?(\\d{2})?"
			+ "(T\\d{2}(\\d{2}(\\d{2}(\\.\\d{3})?)?)?"
			+ "(Z|(\\+|-)\\d{2}(\\d{2})?)?)?";
	String patIsoDateString="-?(\\d{4})(-\\d{2})?(-\\d{2})?"
			+ "(T\\d{2}(:\\d{2}(:\\d{2}(\\.\\d{3})?)?)?"
			+ "(Z|(\\+|-)\\d{2}(:\\d{2})?)?)?";
	
	Pattern patIsoDateBrief=Pattern.compile(patIsoDateBriefString);
	
	Pattern patIsoDate=Pattern.compile(patIsoDateString);
	
	public Match extract(String inputValue) {
		Matcher m=patIsoDate.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.Iso8601Date, inputValue, m.group(2)+" "+m.group(1)+" "+m.group(3));
		 m=patIsoDateBrief.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.Iso8601BriefDate, inputValue, m.group(2)+" "+m.group(1)+" "+m.group(3));
		return null;
	}
	
	
	public static void main(String[] args) throws Exception {
		PatternIso8601Date pat=new PatternIso8601Date();
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
