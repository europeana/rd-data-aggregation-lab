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

import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.dataprocessing.dates.extraction.MatchId;
import europeana.rnd.dataprocessing.dates.stats.HtmlExporter;
import europeana.rnd.dataprocessing.dates.stats.NewspapersIssuedStats;
import europeana.rnd.dataprocessing.dates.stats.ThematicCollectionIssuedStats;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Ore;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptNormalizeDatesThematicCollection {
	
	File inputFolder;
	File outputFolder;
	DatesExtractorHandler handler;
	
	public ScriptNormalizeDatesThematicCollection(File folder, File outFolder) {
		super();
		this.inputFolder = folder;
		this.outputFolder = outFolder;
		this.handler = new DatesExtractorHandler(outFolder);
	}

	public void process() throws IOException {
		ThematicCollectionIssuedStats stats=new ThematicCollectionIssuedStats();
		
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

//					if(ThematicCollections.isFromWw1Collection(choUri)) {
						if(ThematicCollections.isFromArtCollection(choUri)) {
						
						String dataset = EdmRdfUtil.getDatasetFromApiIdOrUri(choUri);
						DatesInRecord record=new DatesInRecord(jv);
						try {
							handler.handle(record);							

							stats.incrementRecord(dataset);
							List<Match> issuedValues = record.getValuesFor(Source.PROVIDER, Ore.Proxy, DcTerms.issued);
							if(issuedValues!=null && !issuedValues.isEmpty()) {
								stats.incrementRecordWithDctermsIssued(dataset);
								for(Match m: issuedValues) {
									if(m.getMatchId()!=MatchId.NO_MATCH && m.getMatchId()!=MatchId.INVALID) {
										stats.incrementRecordWithDctermsIssuedNormalizable(dataset);
										break;
									}
								}
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
//		HtmlExporter.export(stats, outputFolder, "WW1");
		HtmlExporter.export(stats, outputFolder, "Art");
	}


	public static void main(String[] args) throws Exception {
		String sourceFolder = "c://users/nfrei/desktop/data/dates";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolder = args[0];
			}
		}
		
//		File outFolder=new File(sourceFolder+"/extractionWw1");
		File outFolder=new File(sourceFolder+"/extractionArt");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptNormalizeDatesThematicCollection processor=new ScriptNormalizeDatesThematicCollection(new File(sourceFolder), outFolder);
		processor.process();
	}
}
