package europeana.rnd.dataprocessing.dates.extraction;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;

public class PatternBriefDateRange implements DateExtractor {
	Pattern briefDateRangePattern=Pattern.compile("(?<uncertain>\\?\\s*)?(?<start>\\d\\d\\d\\d?)[\\-/](?<end>\\d\\d)(?<uncertain2>\\s*\\?)?");

	public Match extract(String inputValue) {
		Matcher m=briefDateRangePattern.matcher(inputValue.trim()); 
		
		if(m.matches()) {
			Date dStart=new Date();
			dStart.setYear(Integer.parseInt(m.group("start")));
			int endYear=Integer.parseInt(m.group("end"));
			if(endYear>12) {
				int startYear=dStart.getYear() % 100;
				if (startYear<endYear) {
					Date dEnd=new Date();
					dEnd.setYear((dStart.getYear() / 100) * 100 + endYear);
					
					if(m.group("uncertain")!=null || m.group("uncertain2")!=null) {
						dStart.setUncertain(true);
						dEnd.setUncertain(true);
					}
					return new Match(MatchId.Brief_Date_Range, inputValue, new Interval(new Instant(dStart), new Instant(dEnd)));
				}
			}
		}
		return null;
	}

}
