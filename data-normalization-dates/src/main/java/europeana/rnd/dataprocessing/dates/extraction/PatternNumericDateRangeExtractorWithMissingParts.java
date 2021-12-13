package europeana.rnd.dataprocessing.dates.extraction;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternNumericDateRangeExtractorWithMissingParts implements DateExtractor {
	   //period separators "/" -> "[-\\.]"
	   //period separators " - " -> "[-\\./]"
	   //period separators "-" -> "[\\./]"	
//	String dateYyyyMmDdPatStr="(\\d\\d\\d\\d(([-/.]\\d\\d?)?[-/.]\\d\\d?)?|\\?)";
//	String dateDdMmYyyyPatStr="(((\\d\\d?[-/.])?\\d\\d??[-/.])?\\d\\d\\d\\d?|\\?)";
	
	ArrayList<Pattern> patterns=new ArrayList<Pattern>();

	public PatternNumericDateRangeExtractorWithMissingParts() {
		String dateSep="/";
		String componentSep="[-\\.]";
		String dateYmd="\\s*(\\d\\d\\d\\d?(("+componentSep+"\\d\\d?)?"+componentSep+"\\d\\d?)?\\??|\\?)\\s*";
		String dateDmy="\\s*(((\\d\\d?"+componentSep+")?\\d\\d??"+componentSep+")?\\d\\d\\d\\d?\\??|\\?)\\s*";
		patterns.add(Pattern.compile(dateYmd+dateSep+dateYmd));
		patterns.add(Pattern.compile(dateDmy+dateSep+dateDmy));

		dateSep=" - ";
		componentSep="[-\\./]";
		dateYmd="\\s*(\\d\\d\\d\\d?(("+componentSep+"\\d\\d?)?"+componentSep+"\\d\\d?)?\\??|\\?)\\s*";
		dateDmy="\\s*(((\\d\\d?"+componentSep+")?\\d\\d??"+componentSep+")?\\d\\d\\d\\d?\\??|\\?)\\s*";
		patterns.add(Pattern.compile(dateYmd+dateSep+dateYmd));
		patterns.add(Pattern.compile(dateDmy+dateSep+dateDmy));

		dateSep="-";
		componentSep="[\\./]";
		dateYmd="\\s*(\\d\\d\\d\\d?(("+componentSep+"\\d\\d?)?"+componentSep+"\\d\\d?)?\\??|\\?)\\s*";
		dateDmy="\\s*(((\\d\\d?"+componentSep+")?\\d\\d?"+componentSep+")?\\d\\d\\d\\d?\\??|\\?)\\s*";
		patterns.add(Pattern.compile(dateYmd+dateSep+dateYmd));
		patterns.add(Pattern.compile(dateDmy+dateSep+dateDmy));
	}
	
	public Match extract(String inputValue) {
		for(Pattern pat: patterns) {
			Matcher m=pat.matcher(inputValue.trim()); 
			if(m.matches())
				return new Match(MatchId.Numeric_Range_AllVariants, inputValue, m.group(1)+"-"+m.group(2));
		}
		return null;
	}
	
}
