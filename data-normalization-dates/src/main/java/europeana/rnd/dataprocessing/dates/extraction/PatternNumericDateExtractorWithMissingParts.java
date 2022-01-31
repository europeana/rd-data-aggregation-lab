package europeana.rnd.dataprocessing.dates.extraction;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;

public class PatternNumericDateExtractorWithMissingParts implements DateExtractor {
	ArrayList<Pattern> patterns=new ArrayList<Pattern>();
	Pattern cleanSeparator=Pattern.compile("[\\-\\./]");
	
	public PatternNumericDateExtractorWithMissingParts() {
		String componentSep="[\\-\\./]";
		String dateYmd="\\s*(?<uncertain>\\?)?(?<year>\\d\\d\\d\\d?)"
				+ "(?<month>"+componentSep+"\\d\\d?)?(?<day>"+componentSep+"\\d\\d?)?(?<uncertain2>\\?)?\\s*";
		String dateDmy="\\s*(?<uncertain>\\?)?(?<day>\\d\\d?"+componentSep+")?(?<month>\\d\\d?"+componentSep+")?(?<year>\\d\\d\\d\\d?)(?<uncertain2>\\?)?\\s*";
		patterns.add(Pattern.compile(dateYmd));
		patterns.add(Pattern.compile(dateDmy));
	}
	
	public Match extract(String inputValue) {
		for(Pattern pat: patterns) {
			Matcher m=pat.matcher(inputValue); 
			if(m.matches()) {
				Date d=new Date();
				d.setYear(Integer.parseInt(m.group("year")));
				if(m.group("month")!=null && m.group("day")!=null) {
					d.setMonth(Integer.parseInt(clean(m.group("month"))));
					d.setDay(Integer.parseInt(clean(m.group("day"))));
				}else if(m.group("month")!=null) {
					d.setMonth(Integer.parseInt(clean(m.group("month"))));					
				}else if(m.group("day")!=null) {
					d.setMonth(Integer.parseInt(clean(m.group("day"))));										
				}
				if(m.group("uncertain")!=null || m.group("uncertain2")!=null) 
					d.setUncertain(true);
				return new Match(MatchId.Numeric_AllVariants, inputValue, new Instant(d));
			}
		}
		return null;
	}

	private String clean(String group) {
		return cleanSeparator.matcher(group).replaceFirst("");
	}
	
}
