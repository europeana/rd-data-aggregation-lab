package europeana.rnd.dataprocessing.dates.extraction;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import europeana.rnd.dataprocessing.dates.edtf.Instant;

public class PatternNumericDateExtractorWithMissingPartsTest {

	@Test
	void parseDate() throws ParseException {
		PatternNumericDateExtractorWithMissingParts extractor=new PatternNumericDateExtractorWithMissingParts();
		Match match = extractor.extract("2004-01-01");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYear()==2004);
		assert(((Instant)match.getExtracted()).getDate().getMonth()==1);
		assert(((Instant)match.getExtracted()).getDate().getDay()==1);
		assert(!((Instant)match.getExtracted()).getDate().isUncertain());
		
		match = extractor.extract("2004.01.01?");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYear()==2004);
		assert(((Instant)match.getExtracted()).getDate().getMonth()==1);
		assert(((Instant)match.getExtracted()).getDate().getDay()==1);
		assert(((Instant)match.getExtracted()).getDate().isUncertain());

		match = extractor.extract("?2004/01/01");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYear()==2004);
		assert(((Instant)match.getExtracted()).getDate().getMonth()==1);
		assert(((Instant)match.getExtracted()).getDate().getDay()==1);
		assert(((Instant)match.getExtracted()).getDate().isUncertain());

		match = extractor.extract("?2004");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYear()==2004);
		assert(((Instant)match.getExtracted()).getDate().getMonth()==null);
		assert(((Instant)match.getExtracted()).getDate().getDay()==null);
		assert(((Instant)match.getExtracted()).getDate().isUncertain());
	}
}
