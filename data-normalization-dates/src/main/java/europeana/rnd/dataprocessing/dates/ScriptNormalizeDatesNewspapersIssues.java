package europeana.rnd.dataprocessing.dates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import europeana.rnd.dataprocessing.dates.stats.HtmlExporter;
import europeana.rnd.dataprocessing.dates.stats.NewspapersIssuedStats;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptNormalizeDatesNewspapersIssues {
	
	File inputFolder;
	File outputFolder;
	DatesExtractorHandler handler;
	
	public ScriptNormalizeDatesNewspapersIssues(File folder, File outFolder) {
		super();
		this.inputFolder = folder;
		this.outputFolder = outFolder;
		this.handler = new DatesExtractorHandler(outFolder);
	}

	public void process() throws IOException {
		NewspapersIssuedStats stats=new NewspapersIssuedStats();
		
		for(File innerFolder: inputFolder.listFiles()) {
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
					String choUri = jv.getString("id");
					if(ThematicCollections.isFromNewspapersCollection(choUri)) {
						String dataset = EdmRdfUtil.getDatasetFromApiIdOrUri(choUri);
						DatesInRecord record=new DatesInRecord(jv);
						try {
							handler.handle(record);							
							if(record.isNewspaperIssue) {
								stats.incrementIssue(dataset);
								List<Match> issuedValues = record.getValuesFor(Source.PROVIDER, Ore.Proxy, DcTerms.issued);
								if(issuedValues!=null && !issuedValues.isEmpty()) {
									stats.incrementIssueWithDctermsIssued(dataset);
									for(Match m: issuedValues) {
										if(m.getMatchId()!=MatchId.NO_MATCH && m.getMatchId()!=MatchId.INVALID) {
											stats.incrementIssueWithDctermsIssuedNormalizable(dataset);
											break;
										}
									}
								}
							} else if(record.isNewspaperTitle) {
								stats.incrementTitle(dataset);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				is.close();
			}
		}
		handler.close();
		System.out.println("Finished");
		HtmlExporter.export(stats, outputFolder);
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
		
		ScriptNormalizeDatesNewspapersIssues processor=new ScriptNormalizeDatesNewspapersIssues(new File(sourceFolder), outFolder);
		processor.process();
	}
}
