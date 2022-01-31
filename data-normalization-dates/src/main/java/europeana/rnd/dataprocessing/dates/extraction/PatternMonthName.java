package europeana.rnd.dataprocessing.dates.extraction;

import java.time.Month;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;

public class PatternMonthName implements DateExtractor {

	HashMap<Month, Pattern> patternDayMonthYear=new HashMap<Month, Pattern>(12);
	HashMap<Month, Pattern> patternMonthDayYear=new HashMap<Month, Pattern>(12);
	HashMap<Month, Pattern> patternMonthYear=new HashMap<Month, Pattern>(12);
	
	public PatternMonthName() {
		MonthMultilingual months=new MonthMultilingual();
		for(Month month: Month.values()) {
			String monthNamesPattern=null;
			for(String m:months.getMonthStrings(month)) {
				if(monthNamesPattern==null)
					monthNamesPattern="(";
				else
					monthNamesPattern+="|";
				monthNamesPattern+=m.replaceAll("\\.", "\\.");
			}
			monthNamesPattern+=")";

			patternDayMonthYear.put(month, Pattern.compile("[\\s\\[]*(\\d\\d?)[\\s\\w\\.,]{1,4}"+monthNamesPattern+"[\\s\\.,\\w]{1,4}(\\d{4})[\\s\\]]*",Pattern.CASE_INSENSITIVE));
			patternMonthDayYear.put(month, Pattern.compile("[\\s\\[]*"+monthNamesPattern+"[\\s\\.\\w,]{1,4}(\\d\\d?)[\\s\\.,\\w]{1,4}(\\d{4})[\\s\\]]*",Pattern.CASE_INSENSITIVE));
			patternMonthYear.put(month, Pattern.compile("[\\s\\[]*"+monthNamesPattern+"[\\s\\.,\\w]{1,4}(\\d{4})[\\s\\]]*",Pattern.CASE_INSENSITIVE));
		}
		
	}
	
	@Override
	public Match extract(String inputValue) {
		for(Month month: Month.values()) {
			Matcher m=patternDayMonthYear.get(month).matcher(inputValue); 
			if(m.matches()) {
				Date d=new Date();
				d.setYear(Integer.parseInt(m.group(3)));
				d.setMonth(month.getValue());
				d.setDay(Integer.parseInt(m.group(1)));
				return new Match(MatchId.MONTH_NAME_DAY_YEAR, inputValue, new Instant(d));
			}
			m=patternMonthDayYear.get(month).matcher(inputValue); 
			if(m.matches()) {
				Date d=new Date();
				d.setYear(Integer.parseInt(m.group(3)));
				d.setMonth(month.getValue());
				d.setDay(Integer.parseInt(m.group(2)));
				return new Match(MatchId.MONTH_NAME_DAY_YEAR, inputValue, new Instant(d));
			}
			m=patternMonthYear.get(month).matcher(inputValue); 
			if(m.matches()) {
				Date d=new Date();
				d.setYear(Integer.parseInt(m.group(2)));
				d.setMonth(month.getValue());
				return new Match(MatchId.MONTH_NAME_YEAR, inputValue, new Instant(d));
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		new PatternMonthName();
	}
}
