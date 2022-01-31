package europeana.rnd.dataprocessing.dates.stats;

import java.io.File;

import europeana.rnd.dataprocessing.dates.DatesInRecord.DateValue;
import europeana.rnd.dataprocessing.dates.extraction.CleanId;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;

public class ScriptTestDateExtractionStatisticsOutput {

	public static void main(String[] args) throws Exception {
		DateExtractionStatistics des=new DateExtractionStatistics();
		for(int i=0; i<3; i++) {
			for(int j=0; j<200; j++) {
				des.add("http://data.europeana.eu/item/"+i+"/"+j,
						new DateValue("EuropeanaProxy", "date", new Match(MatchId.Century_Numeric, CleanId.INITIAL_TEXT, "date"+(j % 8), null)));
				des.add("http://data.europeana.eu/item/"+i+"/"+j,
						new DateValue("EuropeanaProxy", "subject", new Match(MatchId.Century_Numeric, CleanId.INITIAL_TEXT, "date"+(j % 8), null)));
				des.add("http://data.europeana.eu/item/"+i+"/"+j, 
						new DateValue("ProviderProxy", "date", new Match(MatchId.YYYY, "date"+j, null)) );
				des.add("http://data.europeana.eu/item/"+i+"/"+j, 
						new DateValue("Agent", "dateOfBirth", new Match(MatchId.YYYY, "date"+j, null)) );
			}
		}
		File outputFolder=new File("target");
		des.save(outputFolder);
		
		HtmlExporter.export(des, outputFolder);
		
		System.out.println("Finished");
	}
	
}
