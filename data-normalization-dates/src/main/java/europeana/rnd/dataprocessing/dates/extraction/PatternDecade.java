package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;

public class PatternDecade implements DateExtractor {
	Pattern patYyyy=Pattern.compile("\\s*\\??(\\d\\d\\d)[x\\-\\?]\\??\\s*",Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m=patYyyy.matcher(inputValue); 
		if(!m.matches())
			return null;
		Date d=new Date();
		d.setYearPrecision(YearPrecision.DECADE);
		d.setYear(Integer.parseInt(m.group(1))*10);
		return new Match(MatchId.Decade, inputValue, new Instant(d));
	}
	
}
