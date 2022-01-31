package europeana.rnd.dataprocessing.dates.extraction;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import europeana.rnd.dataprocessing.dates.edtf.Date.YearPrecision;
import europeana.rnd.dataprocessing.dates.edtf.Instant;

public class PatternNumericDateExtractorWithMissingPartsAndXxTest {

	@Test
	void parseDate() throws ParseException {
		PatternNumericDateExtractorWithMissingPartsAndXx extractor=new PatternNumericDateExtractorWithMissingPartsAndXx();
		Match match = extractor.extract("2004-?");
		assert(match==null);
		
		match = extractor.extract("2004/?");
		assert(match==null);

		match = extractor.extract("200u");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYearPrecision()==YearPrecision.DECADE);

		match = extractor.extract("200-");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYearPrecision()==YearPrecision.DECADE);

		match = extractor.extract("20--");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYearPrecision()==YearPrecision.CENTURY);		
		
		match = extractor.extract("2004/--");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYear()==2004);
		
		match = extractor.extract("2004-XX?");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYear()==2004);
		assert(((Instant)match.getExtracted()).getDate().getMonth()==null);
		assert(((Instant)match.getExtracted()).getDate().getDay()==null);
		assert(((Instant)match.getExtracted()).getDate().isUncertain());

		match = extractor.extract("2004/12/30");
		assert(match!=null);
		assert(((Instant)match.getExtracted()).getDate().getYear()==2004);
		assert(((Instant)match.getExtracted()).getDate().getMonth()==12);
		assert(((Instant)match.getExtracted()).getDate().getDay()==30);
		assert(!((Instant)match.getExtracted()).getDate().isUncertain());
	}
}
