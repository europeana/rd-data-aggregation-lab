package europeana.rnd.dataprocessing.dates.extraction.trash;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.Match;
import europeana.rnd.dataprocessing.dates.MatchId;
import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;
import europeana.rnd.dataprocessing.dates.edtf.Time;
import europeana.rnd.dataprocessing.dates.extraction.DateExtractor;

public class PatternIso8601DateRange extends PatternIso8601Date implements DateExtractor {
	Pattern patIsoDateRangeBrief=Pattern.compile(patIsoDateBriefString+"/"+patIsoDateBriefString
			.replace("year", "year2").replace("month", "month2").replace("day", "day2")
			.replace("hour", "hour2").replace("minute", "minute2").replace("second", "second2")
			);
	
	Pattern patIsoDateRange=Pattern.compile(patIsoDateString+"/"+patIsoDateString
			.replace("year", "year2").replace("month", "month2").replace("day", "day2")
			.replace("hour", "hour2").replace("minute", "minute2").replace("second", "second2")
);
	
	public PatternIso8601DateRange(boolean allowBriefFormat) {
		super(allowBriefFormat);
	}
	
	public Match extract(String inputValue) {
		Matcher m=patIsoDateRange.matcher(inputValue); 
		if(m.matches()) {
			Instant start;
			Instant end;
			{
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
				start=new Instant(d,t);
			}			
			{
				Date d=new Date();
				d.setYear(Integer.parseInt(m.group("year2")));
				if(m.group("month2")!=null) {
					d.setMonth(Integer.parseInt(m.group("month2")));
					if(m.group("day2")!=null) 
						d.setDay(Integer.parseInt(m.group("day2")));
				}
				Time t=null;
				if(m.group("hour2")!=null) {
					t=new Time();
					t.setHour(Integer.parseInt(m.group("hour2")));					
					if(m.group("minute2")!=null) {
						t.setMinute(Integer.parseInt(m.group("minute2")));										
						if(m.group("second2")!=null) 
							t.setSecond(Integer.parseInt(m.group("second2")));	
					}
				}
				end=new Instant(d,t);
			}			
			return new Match(MatchId.Iso8601DateRange, inputValue, new Interval(start, end));
		}
		if(allowBriefFormat) {
			 m=patIsoDateRangeBrief.matcher(inputValue); 
			if(m.matches()){
				Instant start;
				Instant end;
				{
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
					start=new Instant(d,t);
				}			
				{
					Date d=new Date();
					d.setYear(Integer.parseInt(m.group("year2")));
					if(m.group("month2")!=null) {
						d.setMonth(Integer.parseInt(m.group("month2")));
						if(m.group("day2")!=null) 
							d.setDay(Integer.parseInt(m.group("day2")));
					}
					Time t=null;
					if(m.group("hour2")!=null) {
						t=new Time();
						t.setHour(Integer.parseInt(m.group("hour2")));					
						if(m.group("minute2")!=null) {
							t.setMinute(Integer.parseInt(m.group("minute2")));										
							if(m.group("second2")!=null) 
								t.setSecond(Integer.parseInt(m.group("second2")));	
						}
					}
					end=new Instant(d,t);
				}			
				return new Match(MatchId.Iso8601BriefDateRange, inputValue, new Interval(start, end));
			}
		}
		return null;
	}
	
	
	public static void main(String[] args) throws Exception {
		PatternIso8601DateRange pat=new PatternIso8601DateRange(true);
		System.out.println(pat.extract("2021-11-09/2021-11-09"));
		System.out.println(pat.extract("2020/2021"));
		System.out.println(pat.extract("-2021/0001"));
	}
}
