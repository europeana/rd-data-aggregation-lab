package europeana.rnd.dataprocessing.dates.extraction;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import europeana.rnd.dataprocessing.dates.edtf.Date;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.TemporalEntity;
import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;
import europeana.rnd.dataprocessing.dates.edtf.EdtfParser;

public class PatternEdtf implements DateExtractor {
	EdtfParser parser=new EdtfParser();
	
	public Match extract(String inputValue) {
		try {
			TemporalEntity parsed = parser.parse(inputValue);
			return new Match(MatchId.Edtf, inputValue, parsed);
		}catch(ParseException | NumberFormatException e) {
			return null;
		}
	}
	
}
