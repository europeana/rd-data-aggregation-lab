package europeana.rnd.dataprocessing.dates.afterimplementation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

public class ScriptAnalisePostImplementationResults {

	File folder;
	ChatGptTestSetHandler handlerChatGpt;
	ProviderTimeSpansHandler handlerProviderTimeSpans;
	
	public ScriptAnalisePostImplementationResults(File folder, File outputFolder) {
		super();
		this.folder = folder;
		this.handlerChatGpt = new ChatGptTestSetHandler(outputFolder);
		this.handlerProviderTimeSpans = new ProviderTimeSpansHandler(outputFolder);
	}

	public void process() throws IOException {
		for(File innerFolder: folder.listFiles()) {
			if(innerFolder.isFile()) continue;
			if(!innerFolder.getName().startsWith("dates_export_afterimplementation_")) continue;
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
					DatesInProviderRecord record=new DatesInProviderRecord(jv);
					try {
						handlerChatGpt.handle(record);
						handlerProviderTimeSpans.handle(record);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				is.close();
			}
		}
		handlerChatGpt.close();
		handlerProviderTimeSpans.close();
	}


	public static void main(String[] args) throws Exception {
		String sourceFolder = "c://users/nfrei/desktop/data/dates-afterimplementation";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolder = args[0];
			}
		}
		File outFolder=new File(sourceFolder+"/extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptAnalisePostImplementationResults processor=new ScriptAnalisePostImplementationResults(new File(sourceFolder), outFolder);
		processor.process();
	}
}
