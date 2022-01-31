package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;

public class PatternBcAd implements DateExtractor {
	String patYearBcAd="(?<year>\\d{2,4})\\s*(?<era>BC|AD|AC|DC)";//|VC|NC
	Pattern patYyyy=Pattern.compile(patYearBcAd, Pattern.CASE_INSENSITIVE);
	Pattern patRange=Pattern.compile(patYearBcAd+"\\s*[\\-\\/]\\s*"+patYearBcAd.replace("<year>", "<year2>").replace("<era>", "<era2>"), Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m=patYyyy.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			if(m.group("era").equals("BC") || m.group("era").equals("AC"))
				d.setYear(- Integer.parseInt(m.group("year")));
			else
				d.setYear(Integer.parseInt(m.group("year")));
			return new Match(MatchId.BcAd, inputValue, new Instant(d));
		}
		m=patRange.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			if(m.group("era").equals("BC") || m.group("era").equals("AC"))
				d.setYear(- Integer.parseInt(m.group("year")));
			else
				d.setYear(Integer.parseInt(m.group("year")));
			Instant start=new Instant(d);
			
			d=new Date();
			if(m.group("era").equals("BC") || m.group("era").equals("AC"))
				d.setYear(- Integer.parseInt(m.group("year")));
			else
				d.setYear(Integer.parseInt(m.group("year")));
			Instant end=new Instant(d);
			
			return new Match(MatchId.BcAd, inputValue, new Interval(start, end));
		}
		return null;
	}
	
}
