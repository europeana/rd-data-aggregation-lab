package europeana.rnd.dataprocessing.dates.stats;

import java.io.File;

import europeana.rnd.dataprocessing.dates.CleanId;
import europeana.rnd.dataprocessing.dates.Match;
import europeana.rnd.dataprocessing.dates.MatchId;
import europeana.rnd.dataprocessing.dates.Source;
import europeana.rnd.dataprocessing.dates.edtf.TemporalEntity;
import europeana.rnd.dataprocessing.dates.DatesInRecord.DateValue;

public class ScriptTestDateExtractionStatisticsOutput {

	public static void main(String[] args) throws Exception {
		DateExtractionStatistics des=new DateExtractionStatistics();
		for(int i=0; i<3; i++) {
			for(int j=0; j<200; j++) {
				des.add("http://data.europeana.eu/item/"+i+"/"+j, Source.PROVIDER,
						new DateValue("Proxy", "date", 
								new Match(MatchId.Century_Numeric, CleanId.INITIAL_TEXT, "date"+(j % 8), (TemporalEntity)null) ));
				des.add("http://data.europeana.eu/item/"+i+"/"+j, Source.PROVIDER,
						new DateValue("Proxy", "issued", new Match(MatchId.Century_Numeric, CleanId.INITIAL_TEXT, "date"+(j % 8), (TemporalEntity)null)));
				des.add("http://data.europeana.eu/item/"+i+"/"+j,  Source.EUROPEANA,
						new DateValue("Proxy", "date", new Match(MatchId.YYYY, "date"+j, (TemporalEntity)null)) );
				des.add("http://data.europeana.eu/item/"+i+"/"+j,  Source.EUROPEANA,
						new DateValue("Agent", "dateOfBirth", new Match(MatchId.YYYY, "date"+j, (TemporalEntity)null)) );
			}
		}
		DateExtractionStatistics desSub=new DateExtractionStatistics();
		for(int i=0; i<3; i++) {
			for(int j=0; j<200; j++) {
				desSub.add("http://data.europeana.eu/item/"+i+"/"+j, Source.PROVIDER,
						new DateValue("Proxy", "subject", new Match(MatchId.Century_Numeric, CleanId.INITIAL_TEXT, "date"+(j % 8), (TemporalEntity)null)));
				desSub.add("http://data.europeana.eu/item/"+i+"/"+j, Source.PROVIDER,
						new DateValue("Proxy", "subject", new Match(MatchId.Century_Numeric, CleanId.INITIAL_TEXT, "date"+(j % 8), (TemporalEntity)null)));
				desSub.add("http://data.europeana.eu/item/"+i+"/"+j,  Source.EUROPEANA,
						new DateValue("Proxy", "coverage", new Match(MatchId.YYYY, "date"+j, (TemporalEntity)null)) );
				desSub.add("http://data.europeana.eu/item/"+i+"/"+j,  Source.EUROPEANA,
						new DateValue("Proxy", "subject", new Match(MatchId.YYYY, "date"+j, (TemporalEntity)null)) );
			}
		}
		File outputFolder=new File("target");
		desSub.save(outputFolder);
		
		HtmlExporter.export(des, desSub, outputFolder);
		
		System.out.println("Finished");
	}
	
}
