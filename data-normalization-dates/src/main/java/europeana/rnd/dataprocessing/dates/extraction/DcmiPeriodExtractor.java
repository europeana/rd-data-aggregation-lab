package europeana.rnd.dataprocessing.dates.extraction;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.format.ISODateTimeFormat;

import europeana.rnd.dataprocessing.dates.edtf.Instant;
import europeana.rnd.dataprocessing.dates.edtf.Interval;

public class DcmiPeriodExtractor implements DateExtractor {
	  private static final Pattern DCMI_PERIOD = Pattern.compile("(start|end|name)\\s*=\\s*(.*?)(?:;|\\s*$)");
	  private static final Pattern DCMI_PERIOD_SCHEME = Pattern.compile("scheme\\s*=\\s*(.*?)(?:;|\\s*$)");
	
	public static DcmiPeriod decodePeriod(String value) {
	    // Parse value
	    Matcher schemeMatcher = DCMI_PERIOD_SCHEME.matcher(value);
	    boolean mayBeW3CDTFEncoded = true;
	    if (schemeMatcher.find()) {
	      String schemeString = schemeMatcher.group(1);
	      if (!"W3C-DTF".equalsIgnoreCase(schemeString) && !"W3CDTF".equalsIgnoreCase(schemeString)) {
	        mayBeW3CDTFEncoded = false;
	      }
	    }
	    try {
	      if (mayBeW3CDTFEncoded) {
	        // Declare fields
	        Date start = null;
	        Date end = null;
	        String name = null;
	        // Parse
	        Matcher m = DCMI_PERIOD.matcher(value);
	        while (m.find()) {
	          String field = m.group(1);
	          String fieldValue = m.group(2);
	          if ("start".equals(field)) {
	            if (start != null)
	              return null;
	            start = parseW3CDTF(fieldValue);
	          } else if ("end".equals(field)) {
	            if (end != null)
	              return null;
	            end = parseW3CDTF(fieldValue);
	          } else if ("name".equals(field)) {
	            if (name != null)
	              return null;
	            name = fieldValue;
	          }
	        }
	        if (start == null && end == null)
	          return null;
	        return new DcmiPeriod(start, end, name);
	      }
	    } catch (IllegalArgumentException ignore) {
	      // Parse error
	    }
	    return null;
	  }
	
	
	  /**
	   * @throws IllegalArgumentException
	   *           if the value cannot be parsed
	   */
	  private static Date parseW3CDTF(String value) {
	    return ISODateTimeFormat.dateTimeParser().parseDateTime(value).toDate();
	  }


	@Override
	public Match extract(String inputValue) {
		try {
			DcmiPeriod decoded = decodePeriod(inputValue);
			if (decoded==null)
				return null;
			
			Instant edtfStart;
			Instant edtfEnd;
			if(decoded.hasStart()) {
				edtfStart=new Instant(decoded.getStart());
			}else
				edtfStart=new Instant(europeana.rnd.dataprocessing.dates.edtf.Date.UNSPECIFIED);
			if(decoded.hasEnd()) {
				edtfEnd=new Instant(decoded.getEnd());
			}else
				edtfEnd=new Instant(europeana.rnd.dataprocessing.dates.edtf.Date.UNSPECIFIED);
			
			Interval edtf=new Interval(edtfStart, edtfEnd);
			
			return new Match(MatchId.DCMIPeriod, inputValue, edtf);
		} catch (IllegalStateException e) {
			//a parsing error occoured
			return null;
		}
	}
}
