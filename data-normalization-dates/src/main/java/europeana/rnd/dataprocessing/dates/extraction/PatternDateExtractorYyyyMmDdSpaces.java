package europeana.rnd.dataprocessing.dates.extraction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;

public class PatternDateExtractorYyyyMmDdSpaces implements DateExtractor {
	Pattern patYyyyMmDd=Pattern.compile("\\s*(\\d{4}) (\\d{1,2}) (\\d{1,2})\\s*");
	Pattern patDdMmYyyy=Pattern.compile("\\s*(\\d{1,2}) (\\d{1,2}) (\\d{4})\\s*");
//	Pattern patYyyyMmDdX=Pattern.compile("\\s*([\\[\\?]{0,2})(\\d\\d\\d\\d)[-/. ](\\d\\d?)[-/. ](\\d\\d?)[\\]\\?]{0,2}\\s*");
	
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
		Matcher m=patYyyyMmDd.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			d.setYear(Integer.parseInt(m.group(1)));
			d.setMonth(Integer.parseInt(m.group(2)));
			d.setDay(Integer.parseInt(m.group(3)));
			return new Match(MatchId.YYYY_MM_DD_Spaces, inputValue, new Instant(d));
		}
		m=patDdMmYyyy.matcher(inputValue); 
		if(m.matches()) {
			Date d=new Date();
			d.setYear(Integer.parseInt(m.group(3)));
			d.setMonth(Integer.parseInt(m.group(2)));
			d.setDay(Integer.parseInt(m.group(1)));
			return new Match(MatchId.YYYY_MM_DD_Spaces, inputValue, new Instant(d));
		}
		return null;
	}
	
}
