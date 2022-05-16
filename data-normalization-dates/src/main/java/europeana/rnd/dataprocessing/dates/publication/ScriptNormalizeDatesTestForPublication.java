package europeana.rnd.dataprocessing.dates.publication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import europeana.rnd.dataprocessing.dates.DatesExtractorHandler;
import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.dataprocessing.dates.DatesInRecord.DatesFromSource;

public class ScriptNormalizeDatesTestForPublication {

	File folder;
	DatesExtractorForPublicationHandler handler;
	RawDataExporter dataExporter;
	
	public ScriptNormalizeDatesTestForPublication(File folder, File outputFolder) {
		super();
		this.folder = folder;
		this.handler = new DatesExtractorForPublicationHandler(outputFolder);
		dataExporter=new RawDataExporter(outputFolder);
//		this.handlerAgents = new DatesAgentHandler(outputFolder);
	}

	public void process() throws IOException {
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
					String choUri = jv.getString("id");
					DatesInRecord record=new DatesInRecord(jv);
					filterValuesForProviderDataOnly(record);
					try {
						dataExporter.export(record);
						handler.handle(record);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				is.close();
			}
		}
		handler.close();
		dataExporter.close();
	}


	private void filterValuesForProviderDataOnly(DatesInRecord record) {
		record.fromEuropeana=new DatesFromSource();
	}

	public static void main(String[] args) throws Exception {
		String sourceFolder = "c://users/nfrei/desktop/data/dates";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolder = args[0];
			}
		}
		File outFolder=new File(sourceFolder+"/extraction-paper");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptNormalizeDatesTestForPublication processor=new ScriptNormalizeDatesTestForPublication(new File(sourceFolder), outFolder);
		processor.process();
	}
}
