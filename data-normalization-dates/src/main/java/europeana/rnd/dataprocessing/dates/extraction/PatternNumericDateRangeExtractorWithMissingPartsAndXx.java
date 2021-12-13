package europeana.rnd.dataprocessing.dates.extraction;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternNumericDateRangeExtractorWithMissingPartsAndXx implements DateExtractor {
	   //period separators "/" -> "[-\\.]"
	   //period separators " - " -> "[-\\./]"
	   //period separators "-" -> "[\\./]"	
//	String dateYyyyMmDdPatStr="(\\d\\d\\d\\d(([-/.]\\d\\d?)?[-/.]\\d\\d?)?|\\?)";
//	String dateDdMmYyyyPatStr="(((\\d\\d?[-/.])?\\d\\d??[-/.])?\\d\\d\\d\\d?|\\?)";
	
	ArrayList<Pattern> patterns=new ArrayList<Pattern>();
	
	public PatternNumericDateRangeExtractorWithMissingPartsAndXx() {
		String dateSep="\\s*[\\/\\|]\\s*";
		String componentSep="[\\-]";
		String componentMissing="[\\d\\?X][\\d\\?X]";
		String dateYmd="(\\d\\d"+componentMissing+"?(("+componentSep+componentMissing+"?)?"+componentSep+componentMissing+"?)?|\\?)";
		String dateDmy="((("+componentMissing+"?"+componentSep+")?"+componentMissing+"?"+componentSep+")?\\d\\d"+componentMissing+"?|\\?)";
		patterns.add(Pattern.compile(dateYmd+dateSep+dateYmd));
		patterns.add(Pattern.compile(dateDmy+dateSep+dateDmy));

		componentSep="[\\.]";
		componentMissing="[\\-\\d\\?X][\\-\\d\\?X]";
		dateYmd="(\\d\\d"+componentMissing+"?(("+componentSep+componentMissing+"?)?"+componentSep+componentMissing+"?)?|\\?)";
		dateDmy="((("+componentMissing+"?"+componentSep+")?"+componentMissing+"?"+componentSep+")?\\d\\d"+componentMissing+"?|\\?)";
		patterns.add(Pattern.compile(dateYmd+dateSep+dateYmd));
		patterns.add(Pattern.compile(dateDmy+dateSep+dateDmy));

		dateSep="\\s+[\\-\\|]\\s+";
		componentSep="[\\./]";
		componentMissing="[\\-\\d\\?X][\\-\\d\\?X]";
		dateYmd="(\\d\\d"+componentMissing+"?(("+componentSep+componentMissing+"?)?"+componentSep+componentMissing+"?)?|\\?)";
		dateDmy="((("+componentMissing+"?"+componentSep+")?"+componentMissing+"?"+componentSep+")?\\d\\d"+componentMissing+"?|\\?)";
		patterns.add(Pattern.compile(dateYmd+dateSep+dateYmd));
		patterns.add(Pattern.compile(dateDmy+dateSep+dateDmy));

		dateSep="\\s+-\\s+";
		componentSep="[\\-]";
		componentMissing="[\\d\\?X][\\d\\?X]";
		dateYmd="(\\d\\d"+componentMissing+"?(("+componentSep+componentMissing+"?)?"+componentSep+componentMissing+"?)?|\\?)";
		dateDmy="((("+componentMissing+"?"+componentSep+")?"+componentMissing+"?"+componentSep+")?\\d\\d"+componentMissing+"?|\\?)";
		patterns.add(Pattern.compile(dateYmd+dateSep+dateYmd));
		patterns.add(Pattern.compile(dateDmy+dateSep+dateDmy));
		
		dateSep="-";
		componentSep="[\\./]";
		componentMissing="[\\-\\d\\?X][\\-\\d\\?X]";
		dateYmd="(\\d\\d"+componentMissing+"?(("+componentSep+componentMissing+"?)?"+componentSep+componentMissing+"?)?|\\?)";
		dateDmy="((("+componentMissing+"?"+componentSep+")?"+componentMissing+"?"+componentSep+")?\\d\\d"+componentMissing+"?|\\?)";
		patterns.add(Pattern.compile(dateYmd+dateSep+dateYmd));
		patterns.add(Pattern.compile(dateDmy+dateSep+dateDmy));
	}
	
	public Match extract(String inputValue) {
		for(Pattern pat: patterns) {
			Matcher m=pat.matcher(inputValue); 
			if(m.matches())
				return new Match(MatchId.Numeric_Range_AllVariants_Xx, inputValue, m.group(1)+"-"+m.group(2));
		}
		return null;
	}
	
}
