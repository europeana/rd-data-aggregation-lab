package europeana.rnd.dataprocessing.dates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import inescid.util.europeana.EdmRdfUtil;

public class ScriptNormalizeDatesNewspapersReport {
	HashSet<String> datasets=new HashSet<String>() {{
		add("9200338");
		add("15");
		add("2064915");
		add("9200204");
		add("0940443");
		add("9200236");
		add("0940423");
		add("9200355");
		add("2022091");
		add("9200360");
		add("2058617");
		add("0940427");
		add("9200333");
		add("0940416");
		add("9200302");
		add("9200384");
		add("9200375");
		add("9200115");
		add("9200396");
		add("08547");
		add("401");
		add("9200300");
		add("0940406");
		add("9200231");
		add("2021657");
		add("0940442");
		add("2020128");
		add("9200357");
		add("9200303");
		add("9200385");
		add("9200367");
		add("317");
		add("122");
		add("124");
		add("314");
		add("0940424");
		add("18");
		add("205");
		add("9200408");
		add("2058616");
		add("0940432");
		add("2022089");
		add("9200361");
		add("9200339");
		add("9200356");
		add("9200301");
		add("9200359");
		add("0940438");
		add("2021010");
		add("0940417");

	}};
	
	File folder;
	DatesExtractorHandler handler;
	
	public ScriptNormalizeDatesNewspapersReport(File folder, DatesExtractorHandler handler) {
		super();
		this.folder = folder;
		this.handler = handler;
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
					
					//check here
					String dataset = EdmRdfUtil.getDatasetFromApiIdOrUri(jv.getString("id"));
//					System.out.println(jv.getString("id"));
//					System.out.println(dataset);
					if(datasets.contains(dataset)) {
						DatesInRecord record=new DatesInRecord(jv);
						try {
							handler.handle(record);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				is.close();
			}
		}
		handler.close();
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
		
		ScriptNormalizeDatesNewspapersReport processor=new ScriptNormalizeDatesNewspapersReport(new File(sourceFolder), new DatesExtractorHandler(outFolder));
		processor.process();
	}
}
