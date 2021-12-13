package europeana.rnd.dataprocessing.dates;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.FileWriterWithEncoding;

import europeana.rnd.dataprocessing.dates.extraction.CleanId;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfMapsOfInts;
import inescid.util.datastruct.MapOfSets;

public class DateExtractionStatistics {
	MapOfMapsOfInts<String, MatchId> statsByCollection;
//	MapOfMapsOfInts<String, MatchId> statsByProperty;
//	MapOfMapsOfInts<String, MatchId> statsByClass;
//	HashMap<String, MapOfMapsOfInts<String, MatchId>> statsByClassProperty;
	MapOfInts<MatchId> statsGlobal;
	MapOfInts<CleanId> statsGlobalClean;

	public DateExtractionStatistics() {
		statsByCollection=new MapOfMapsOfInts<String, MatchId>();
//		statsByProperty=new MapOfMapsOfInts<String, MatchId>();
//		statsByClass=new MapOfMapsOfInts<String, MatchId>();
//		statsByClassProperty=new HashMap<String, MapOfMapsOfInts<String,MatchId>>();
		statsGlobal=new MapOfInts<MatchId>();
		statsGlobalClean=new MapOfInts<CleanId>();
	}
	
	static final int lengthOfChoUriPrefix = "http://data.europeana.eu/item/".length();
	public void add(String choUri, Match match) {
		String collection=choUri.substring(lengthOfChoUriPrefix, choUri.indexOf('/', lengthOfChoUriPrefix));
		statsByCollection.incrementTo(collection, match.getMatchId());
		statsGlobal.incrementTo(match.getMatchId());
		if(match.getCleanOperation()!=null)
			statsGlobalClean.incrementTo(match.getCleanOperation());
	}
	
	public void save(File globalToFile, File globalCleanToFile, File collectionsToFile) throws IOException {
//		statsGlobal.writeCsv(statsGlobal, null);
		FileWriterWithEncoding writer=new FileWriterWithEncoding(globalToFile, StandardCharsets.UTF_8);
		MapOfInts.writeCsv(statsGlobal, writer);
		writer.close();
		writer=new FileWriterWithEncoding(globalCleanToFile, StandardCharsets.UTF_8);
		MapOfInts.writeCsv(statsGlobalClean, writer);
		writer.close();
		writer=new FileWriterWithEncoding(collectionsToFile, StandardCharsets.UTF_8);
		MapOfMapsOfInts.writeCsv(statsByCollection, writer);
		writer.close();
	}
	
	
	
	public static void main(String[] args) throws Exception {
		DateExtractionStatistics des=new DateExtractionStatistics();
		
		for(int i=0; i<10; i++) {
			for(int j=0; j<10; j++) {
				des.add("http://data.europeana.eu/item/"+i+"/"+j, new Match(MatchId.Century_Numeric, CleanId.INITIAL_TEXT, "", "") );
				des.add("http://data.europeana.eu/item/"+i+"/"+j, new Match(MatchId.YYYY, "", "") );
			}
		}
		File outputFolder=new File("target");
		File outGlobalStats=new File(outputFolder, "stats-global.csv");
		File outGlobalStatsClean=new File(outputFolder, "stats-global-clean.csv");
		File outColStats=new File(outputFolder, "stats-collections.csv");
		des.save(outGlobalStats, outGlobalStatsClean, outColStats);
	}
	
}
