package europeana.rnd.dataprocessing.dates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptNormalizeDatesNewspapersIssues {
	 
	
	public static class DatesInIssuesStats {
		int titlesCount=0;
		int issuesCount=0;
		int issuesWithDctermsIssued=0;
		int issuesWithDctermsIssuedNormalizable=0;
		@Override
		public String toString() {
			return "DatesInIssuesStats [titlesCount=" + titlesCount + ", issuesCount=" + issuesCount
					+ ", issuesWithDctermsIssued=" + issuesWithDctermsIssued + ", issuesWithDctermsIssuedNormalizable="
					+ issuesWithDctermsIssuedNormalizable + "]";
		}		
	}
	
	File folder;
	DatesExtractorHandler handler;
	
	public ScriptNormalizeDatesNewspapersIssues(File folder, DatesExtractorHandler handler) {
		super();
		this.folder = folder;
		this.handler = handler;
	}

	public void process() throws IOException {
		DatesInIssuesStats stats=new DatesInIssuesStats();
		
		for(File innerFolder: folder.listFiles()) {
			if(innerFolder.isFile()) continue;
			if(!innerFolder.getName().startsWith("dates_export_")) continue;
			for(File jsonFile: innerFolder.listFiles()) {
				if(jsonFile.isDirectory()) continue;
				System.out.println(jsonFile.getAbsolutePath());
				FileInputStream is = new FileInputStream(jsonFile);
				JsonParser parser = Json.createParser(is);
				parser.next();
				Stream<JsonValue> arrayStream = parser.getArrayStream();
				for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
					JsonObject jv=it.next().asJsonObject();
					//check here
					if(NewspaperCollection.isFromNewspapersCollection(jv.getString("id"))) {
						DatesInRecord record=new DatesInRecord(jv);
						try {
							if(record.isNewspaperIssue) {
								stats.issuesCount++;
								handler.handle(record);							
								List<Match> issuedValues = record.getValuesFor(Source.PROVIDER, Ore.Proxy, DcTerms.issued);
								if(issuedValues!=null && !issuedValues.isEmpty()) {
									stats.issuesWithDctermsIssued++;
									for(Match m: issuedValues) {
										if(m.getMatchId()!=MatchId.NO_MATCH && m.getMatchId()!=MatchId.INVALID) {
											stats.issuesWithDctermsIssuedNormalizable++;									
											break;
										}
									}
								}
							} else if(record.isNewspaperTitle) {
								stats.titlesCount++;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				is.close();
				System.out.println(stats);
			}
		}
		handler.close();
		System.out.println("Finished");
		System.out.println(stats);
	}


	public static void main(String[] args) throws Exception {
		String sourceFolder = "c://users/nfrei/desktop/data/dates";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolder = args[0];
			}
		}
		
		File outFolder=new File(sourceFolder+"/extractionNewspapers");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptNormalizeDatesNewspapersIssues processor=new ScriptNormalizeDatesNewspapersIssues(new File(sourceFolder), new DatesExtractorHandler(outFolder));
		processor.process();
	}
}
