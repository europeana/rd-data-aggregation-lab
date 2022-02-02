package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMonthName implements DateExtractor {
	MonthMultilingual months=new MonthMultilingual();
	
	Pattern patternDayMonthYear;
	Pattern patternMonthDayYear;
	Pattern patternMonthYear;
	
	public PatternMonthName() {
		String monthNamesPattern=null;
		for(String m:months.getAllMonthStrings()) {
			if(monthNamesPattern==null)
				monthNamesPattern="(";
			else
				monthNamesPattern+="|";
			monthNamesPattern+=m.replaceAll("\\.", "\\.");
		}
		monthNamesPattern+=")";
		
		patternDayMonthYear=Pattern.compile("[\\s\\[]*(\\d\\d?)[\\s\\w\\.,]{1,4}"+monthNamesPattern+"[\\s\\.,\\w]{1,4}(\\d{4})[\\s\\]]*",Pattern.CASE_INSENSITIVE);
		patternMonthDayYear=Pattern.compile("[\\s\\[]*"+monthNamesPattern+"[\\s\\.\\w,]{1,4}(\\d\\d?)[\\s\\.,\\w]{1,4}(\\d{4})[\\s\\]]*",Pattern.CASE_INSENSITIVE);
		patternMonthYear=Pattern.compile("[\\s\\[]*"+monthNamesPattern+"[\\s\\.,\\w]{1,4}(\\d{4})[\\s\\]]*",Pattern.CASE_INSENSITIVE);
		
	}
	
	@Override
	public Match extract(String inputValue) {
		Matcher m=patternDayMonthYear.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.MONTH_NAME_DAY_YEAR, inputValue, m.group(1)+" "+m.group(2)+" "+m.group(3));
		m=patternMonthDayYear.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.MONTH_NAME_DAY_YEAR, inputValue, m.group(2)+" "+m.group(1)+" "+m.group(3));
		m=patternMonthYear.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.MONTH_NAME_YEAR, inputValue, m.group(1)+" "+m.group(2));
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		new PatternMonthName();
	}
}
