package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;

public class PatternDecade implements DateExtractor {
	Pattern patUncertainBegining=Pattern.compile("\\s*(?<uncertain>\\?)?(?<year>\\d\\d\\d)[x\\-\\?xu]\\s*",Pattern.CASE_INSENSITIVE);
	Pattern patUncertainEnding=Pattern.compile("\\s*(?<year>\\d\\d\\d)([x\\-ux](?<uncertain>\\?)?|\\?(?<uncertain2>\\?))\\s*",Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m=patUncertainEnding.matcher(inputValue); 
		if(m.matches()) { 
			Date d=new Date();
			d.setYearPrecision(YearPrecision.DECADE);
			d.setYear(Integer.parseInt(m.group("year"))*10);
			if(m.group("uncertain")!=null || m.group("uncertain2")!=null)
				d.setUncertain(true);
			return new Match(MatchId.Decade, inputValue, new Instant(d));
		}
		m=patUncertainBegining.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			d.setYearPrecision(YearPrecision.DECADE);
			d.setYear(Integer.parseInt(m.group("year"))*10);
			if(m.group("uncertain")!=null)
				d.setUncertain(true);
			return new Match(MatchId.Decade, inputValue, new Instant(d));
		}
		return null;
	}
	
}
