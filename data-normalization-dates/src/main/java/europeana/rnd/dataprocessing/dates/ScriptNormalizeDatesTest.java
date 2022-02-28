package europeana.rnd.dataprocessing.dates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

public class ScriptNormalizeDatesTest {

	File folder;
	DatesExtractorHandler handler;
	DatesAgentHandler handlerAgents;
	
	public ScriptNormalizeDatesTest(File folder, File outputFolder) {
		super();
		this.folder = folder;
		this.handler = new DatesExtractorHandler(outputFolder);
		this.handlerAgents = new DatesAgentHandler(outputFolder);
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
					DatesInRecord record=new DatesInRecord(jv);
					try {
						handler.handle(record);
						handlerAgents.handle(record);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				is.close();
			}
		}
		handler.close();
		handlerAgents.close();
	}


	public static void main(String[] args) throws Exception {
		String sourceFolder = "c://users/nfrei/desktop/data/dates";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolder = args[0];
			}
		}
		File outFolder=new File(sourceFolder+"/extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptNormalizeDatesTest processor=new ScriptNormalizeDatesTest(new File(sourceFolder), outFolder);
		processor.process();
	}
}
