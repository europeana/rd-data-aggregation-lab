package europeana.rnd.dataprocessing.pid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

public class ScriptIdsReport {

	File folder;
	File outputFolder;
	
	IdStats stats=new IdStats();
	
	public ScriptIdsReport(File folder, File outputFolder) {
		super();
		this.folder = folder;
		this.outputFolder = outputFolder;
	}

	public void process() throws IOException {
		if(folder.getName().endsWith(".zip")) {
			try (FileInputStream fis = new FileInputStream(folder);
	                BufferedInputStream bis = new BufferedInputStream(fis);
	                ZipInputStream zis = new ZipInputStream(bis)) {
				ZipEntry ze;
	            while ((ze = zis.getNextEntry()) != null) {
	                if(ze.isDirectory()) continue;
					System.out.println(ze.getName());
					processFile(zis);
	            }
			}
		} else {
			for(File innerFolder: folder.listFiles()) {
				if(innerFolder.isFile()) continue;
				if(!innerFolder.getName().startsWith("ids_export_")) continue;
				for(File jsonFile: innerFolder.listFiles()) {
					if(jsonFile.isDirectory()) continue;
					System.out.println(jsonFile.getAbsolutePath());
					FileInputStream is = new FileInputStream(jsonFile);
					processFile(is);
					is.close();
				}
			}
		}
		System.out.println(stats);
//		HtmlExporter.export(stats, outputFolder);
	}

	private void processFile(InputStream is) {
		JsonParser parser = Json.createParser(is);
		parser.next();
		Stream<JsonValue> arrayStream = parser.getArrayStream();
		for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
			JsonObject jv=it.next().asJsonObject();
			IdsInRecord record=new IdsInRecord(jv);
			try {
//				System.out.println(record);
				for(String id: record.getAllValues()) 
					stats.add(id);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		String sourceFolderStr = "c://users/nfrei/desktop/data/pid";
//		String sourceFolderStr = "c://users/nfrei/desktop/data/pid/ids-export.zip";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		File outFolder=new File(sourceFolderStr.endsWith(".zip") ? sourceFolder.getParentFile() : sourceFolder, "extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptIdsReport processor=new ScriptIdsReport(sourceFolder, outFolder);
		processor.process();
	}
	
	

	
	
}
