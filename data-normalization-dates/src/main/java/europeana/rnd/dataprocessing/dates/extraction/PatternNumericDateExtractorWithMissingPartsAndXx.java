package europeana.rnd.dataprocessing.dates.extraction;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternNumericDateExtractorWithMissingPartsAndXx implements DateExtractor {
	ArrayList<Pattern> patterns=new ArrayList<Pattern>();

	public PatternNumericDateExtractorWithMissingPartsAndXx() {
		String componentSep="[-\\./]";
		String dateYmd="\\??\\d\\d[\\d\\-\\?X][\\d\\-\\?X]?(("+componentSep+"[\\d\\-\\?X][\\d\\-\\?X]?)?"+componentSep+"[\\d\\-\\?X][\\d\\-\\?X]?)?\\??";
		String dateDmy="\\??(([\\d\\-\\?X][\\d\\-\\?X]?"+componentSep+")?[\\d\\-\\?X][\\d\\-\\?X]?"+componentSep+")?\\d\\d[\\d\\-\\?X][\\d\\-\\?X]?\\??";
		patterns.add(Pattern.compile(dateYmd,Pattern.CASE_INSENSITIVE));
		patterns.add(Pattern.compile(dateDmy,Pattern.CASE_INSENSITIVE));
	}
	
	public Match extract(String inputValue) {
		for(Pattern pat: patterns) {
			Matcher m=pat.matcher(inputValue); 
			if(m.matches())
				return new Match(MatchId.Numeric_AllVariants_Xx, inputValue, m.group(1)+"-"+m.group(2));
		}
		return null;
	}
	
}
