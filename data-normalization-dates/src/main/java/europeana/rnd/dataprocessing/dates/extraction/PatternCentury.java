package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;

public class PatternCentury implements DateExtractor {
	Pattern patYyyy=Pattern.compile("\\s*\\??(\\d{2})\\.{2}\\??\\s*",Pattern.CASE_INSENSITIVE);
	Pattern patRoman=Pattern.compile("\\s*(s\\.?|sec\\.?)\\s*([XIV]{1,5})\\s*",Pattern.CASE_INSENSITIVE);
//	Pattern patYyyyX=Pattern.compile("\\s*([\\[\\?]{0,2})(\\d\\d)xx[\\]\\?]{0,2}\\s*",Pattern.CASE_INSENSITIVE);
	Pattern patRomanClean=Pattern.compile("\\s*(I{1,3}|IV|VI{0,3}|I?X|XI{1,3}|XIV|XVI{0,3}|I?XX|XXI)\\s*",Pattern.CASE_INSENSITIVE);

	Pattern patRomanRange=Pattern.compile("\\s*(s\\.?|sec\\.?)\\s*([XIV]{1,5})\\s*"+"\\-"+
			"\\s*([XIV]{1,5})\\s*"
			,Pattern.CASE_INSENSITIVE);
	
	public Match extract(String inputValue) {
		Matcher m; 
		m=patYyyy.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			d.setYearPrecision(YearPrecision.CENTURY);
			d.setYear(Integer.parseInt(m.group(1))*100);
			return new Match(MatchId.Century_Numeric, inputValue, new Instant(d));
		}
		m=patRoman.matcher(inputValue);
		if(m.matches()) {
			Date d=new Date();
			d.setYearPrecision(YearPrecision.CENTURY);
			d.setYear((RomanToNumber.romanToDecimal(m.group(2))-1)*100);
			return new Match(MatchId.Century_Roman, inputValue, new Instant(d));
		}
		m=patRomanClean.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			d.setYearPrecision(YearPrecision.CENTURY);
			d.setYear((RomanToNumber.romanToDecimal(m.group(1))-1)*100);
			return new Match(MatchId.Century_Roman, inputValue, new Instant(d));
		}
		m=patRomanRange.matcher(inputValue); 
		if(m.matches()) {
			Date start=new Date();
			start.setYearPrecision(YearPrecision.CENTURY);
			start.setYear((RomanToNumber.romanToDecimal(m.group(2))-1)*100);
			Date end=new Date();
			end.setYearPrecision(YearPrecision.CENTURY);
			end.setYear((RomanToNumber.romanToDecimal(m.group(3))-1)*100);
			return new Match(MatchId.Century_Range_Roman, inputValue, new Interval(new Instant(start), new Instant(end)));
		}
		return null;
	}
	
}
