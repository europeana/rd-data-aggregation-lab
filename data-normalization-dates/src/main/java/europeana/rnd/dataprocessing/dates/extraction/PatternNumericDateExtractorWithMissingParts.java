package europeana.rnd.dataprocessing.dates.extraction;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternNumericDateExtractorWithMissingParts implements DateExtractor {
	ArrayList<Pattern> patterns=new ArrayList<Pattern>();

	public PatternNumericDateExtractorWithMissingParts() {
		String componentSep="[-\\./]";
		String dateYmd="\\s*\\??\\d\\d\\d\\d?(("+componentSep+"\\d\\d?)?"+componentSep+"\\d\\d?)?\\??\\s*";
		String dateDmy="\\s*\\??((\\d\\d?"+componentSep+")?\\d\\d?"+componentSep+")?\\d\\d\\d\\d?\\??\\s*";
		patterns.add(Pattern.compile(dateYmd));
		patterns.add(Pattern.compile(dateDmy));
	}
	
	public Match extract(String inputValue) {
		for(Pattern pat: patterns) {
			Matcher m=pat.matcher(inputValue); 
			if(m.matches())
				return new Match(MatchId.Numeric_AllVariants, inputValue, m.group(1)+"-"+m.group(2));
		}
		return null;
	}
	
}
