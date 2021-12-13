package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternDateExtractorYyyy implements DateExtractor {
	Pattern patYyyy=Pattern.compile("\\s*(\\d\\d\\d\\d)\\s*");
	Pattern patYyyyX=Pattern.compile("\\s*([\\[\\?]{0,2})(\\d\\d\\d\\d)[\\]\\?]{0,2}\\s*");
	
//    (: [YYYY] :)
//    '^\s*[\[(]\d{4}[\])][^?\w]*$'
//
// (: YYYY, YYYY-MM, YYYY-MM-DD :)
//  , '^\s*\d{4}(\s*[-/.]\d{1,2}){0,2}[^?\w]*$'
//
// (: MM-YYYY, DD-MM-YYYY :)
//  , '^(\s*\d{1,2}\s*[-/.]){1,2}\s*\d{4}[^?\w]*$'
//
// (: Annotated dates :)
//  , '^\s*\d{4}([-/.]\d{1,2}){0,2}\s*\[[Pp]ublication\][^?\w]*$'
//  , '^\s*\d{4}([-/.]\d{1,2}){0,2}\s*\(first performance\)[^?\w]*$'
//
// (: RANGES :)
//  , '^\s*\d{4}\s*[-=/.]\s*\d{4}[^?\w]*$'
//  , '^\s*\d{4}[-/.]\d{1,2}\s*[-=/]\s*\d{4}[-/.]\d{1,2}[^?\w]*$'
//  , '^\s*\d{4}[-/.]\d{1,2}[-/.]\d{1,2}\s*[-=/]\s*\d{4}[-/.]\d{1,2}[-/.]\d{1,2}[^?\w]*$'
//
// (: TIMESTAMPS: 2011-11-29T14:49:21Z :)
//  , '^\s*\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}Z?[^?\w]*$'
//  )"	
	
	
	
	public Match extract(String inputValue) {
		Matcher m=patYyyy.matcher(inputValue); 
		if(m.matches())
			return new Match(MatchId.YYYY, inputValue, m.group(1));
		m=patYyyyX.matcher(inputValue); 
		if(m.matches()) 
			return new Match(MatchId.xYYYYx, inputValue, m.group(2));
		return null;
	}
	
}
