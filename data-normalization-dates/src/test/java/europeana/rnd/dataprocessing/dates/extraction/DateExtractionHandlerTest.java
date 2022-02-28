package europeana.rnd.dataprocessing.dates.extraction;

import java.text.ParseException;

import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.Test;

import europeana.rnd.dataprocessing.dates.DatesExtractorHandler;
import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.dataprocessing.dates.Source;
import europeana.rnd.dataprocessing.dates.edtf.Instant;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.RdfUtil.Jena;

public class DateExtractionHandlerTest {

	@Test
	void parseDate() throws Exception {
		DatesExtractorHandler handler=new DatesExtractorHandler();
		
		DatesInRecord testRec=new DatesInRecord("http://data.europeana.eu/item/test/test", false, false);
		testRec.addTo(Source.PROVIDER, Ore.Proxy, Dc.date, ResourceFactory.createLangLiteral("1972/10/31 | 1972/10/01", null));
		handler.handle(testRec);
	}
}
