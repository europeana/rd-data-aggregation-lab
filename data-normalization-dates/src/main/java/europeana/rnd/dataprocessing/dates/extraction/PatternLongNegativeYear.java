package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;

public class PatternLongNegativeYear implements DateExtractor {
	Pattern patYyyy=Pattern.compile("\\s*(?<uncertain>\\?)?(?<year>\\-\\d{5,9})(?<uncertain2>\\?)?\\s*",Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m; 
		m=patYyyy.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			d.setYear(Integer.parseInt(m.group("year")));
			if(m.group("uncertain")!=null || m.group("uncertain2")!=null)
				d.setUncertain(true);
			return new Match(MatchId.LongYear, inputValue, new Instant(d));
		}
		return null;
	}
	
}
