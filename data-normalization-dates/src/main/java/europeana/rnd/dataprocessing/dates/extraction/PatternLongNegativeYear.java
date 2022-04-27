package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;

public class PatternLongNegativeYear implements DateExtractor {
	Pattern patYyyyyy=Pattern.compile("\\s*(?<uncertain>\\?)?(?<year>\\-\\d{5,9})(?<uncertain2>\\?)?\\s*",Pattern.CASE_INSENSITIVE);
	Pattern patYyyyyyRange=Pattern.compile("\\s*(?<uncertain>\\?)?(?<year>\\-\\d{5,9})\\s*/\\s*(?<year2>\\-\\d{5,9})(?<uncertain2>\\?)?\\s*",Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m; 
		m=patYyyyyy.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			d.setYear(Integer.parseInt(m.group("year")));
			if(m.group("uncertain")!=null || m.group("uncertain2")!=null)
				d.setUncertain(true);
			return new Match(MatchId.LongYear, inputValue, new Instant(d));
		}
		m=patYyyyyyRange.matcher(inputValue); 
		if(m.matches()) {
			Date start=new Date();
			start.setYear(Integer.parseInt(m.group("year")));
			Date end=new Date();
			end.setYear(Integer.parseInt(m.group("year2")));
			Interval interval = new Interval(new Instant(start), new Instant(end));			
			if(m.group("uncertain")!=null || m.group("uncertain2")!=null)
				interval.setUncertain(true);
			return new Match(MatchId.LongYear, inputValue, interval);
		}
		return null;
	}
	
}
