package europeana.rnd.dataprocessing.dates.extraction.trash;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Time;
import europeana.rnd.dataprocessing.dates.extraction.DateExtractor;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;

public class PatternIso8601Date implements DateExtractor {
	String patIsoDateBriefString="(?<year>-?\\d{4})(?<month>\\d{2})?(?<day>\\d{2})?"
			+ "(T(?<hour>\\d{2})((?<minute>\\d{2})((?<second>\\d{2})(\\.\\d{3})?)?)?"
			+ "(Z|(\\+|-)\\d{2}(\\d{2})?)?)?";
	String patIsoDateString="(?<year>-?\\d{4})(-(?<month>\\d{2}))?(-(?<day>\\d{2}))?"
			+ "(T(?<hour>\\d{2})(:(?<minute>\\d{2})(:(?<second>\\d{2})(\\.\\d{3})?)?)?"
			+ "(Z|(\\+|-)\\d{2}(:\\d{2})?)?)?";
	
	Pattern patIsoDateBrief=Pattern.compile(patIsoDateBriefString);
	
	Pattern patIsoDate=Pattern.compile(patIsoDateString);
	
	boolean allowBriefFormat;
	
	public PatternIso8601Date(boolean allowBriefFormat) {
		this.allowBriefFormat = allowBriefFormat;
	}
	
	public Match extract(String inputValue) {
		Matcher m=patIsoDate.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			d.setYear(Integer.parseInt(m.group("year")));
			if(m.group("month")!=null) {
				d.setMonth(Integer.parseInt(m.group("month")));
				if(m.group("day")!=null) 
					d.setDay(Integer.parseInt(m.group("day")));
			}
			Time t=null;
			if(m.group("hour")!=null) {
				t=new Time();
				t.setHour(Integer.parseInt(m.group("hour")));					
				if(m.group("minute")!=null) {
					t.setMinute(Integer.parseInt(m.group("minute")));										
					if(m.group("second")!=null) 
						t.setSecond(Integer.parseInt(m.group("second")));	
				}
			}
			return new Match(MatchId.Iso8601Date, inputValue, new Instant(d,t));
		}
		if(allowBriefFormat) {
			m=patIsoDateBrief.matcher(inputValue); 
			if(m.matches()) {
				Date d=new Date();
				d.setYear(Integer.parseInt(m.group("year")));
				if(m.group("month")!=null) {
					d.setMonth(Integer.parseInt(m.group("month")));
					if(m.group("day")!=null) 
						d.setDay(Integer.parseInt(m.group("day")));
				}
				Time t=null;
				if(m.group("hour")!=null) {
					t=new Time();
					t.setHour(Integer.parseInt(m.group("hour")));					
					if(m.group("minute")!=null) {
						t.setMinute(Integer.parseInt(m.group("minute")));										
						if(m.group("second")!=null) 
							t.setSecond(Integer.parseInt(m.group("second")));		
					}
				}
				return new Match(MatchId.Iso8601BriefDate, inputValue, new Instant(d,t));
			}
		}
		return null;
	}
	
	
	public static void main(String[] args) throws Exception {
		PatternIso8601Date pat=new PatternIso8601Date(true);
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
