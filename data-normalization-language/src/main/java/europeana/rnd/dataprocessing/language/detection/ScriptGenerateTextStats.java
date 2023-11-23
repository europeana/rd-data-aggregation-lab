package europeana.rnd.dataprocessing.language.detection;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

import org.apache.commons.io.FileUtils;

import inescid.util.europeana.EdmRdfUtil;

public class ScriptGenerateTextStats {

	File folder;
	File outputFolder;
	
	TextStats stats=new TextStats();
	int recCount=0;

	public ScriptGenerateTextStats(File folder, File outputFolder) {
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
				if(!innerFolder.getName().startsWith("text_export_")) continue;
				for(File jsonFile: innerFolder.listFiles()) {
					if(jsonFile.isDirectory()) continue;
					System.out.println(jsonFile.getAbsolutePath());
					FileInputStream is = new FileInputStream(jsonFile);
					processFile(is);
					is.close();
				}
			}
		}
	}
	public void saveResult() throws IOException {
		System.out.println(recCount+" records processed");
		stats.saveCsvs(outputFolder);
	}

	private void processFile(InputStream is) {
		try {
			JsonParser parser = Json.createParser(is);
			parser.next();
			Stream<JsonValue> arrayStream = parser.getArrayStream();
			for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
				JsonObject jv=it.next().asJsonObject();
				TextInRecord record=new TextInRecord(jv);
				recCount++;
				try {
					stats.add(record);
//				stats.add(null, null, null);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}
		} catch (javax.json.stream.JsonParsingException e) {
			System.out.println("JSON parsing error. Ignoring the rest of the file.");
			e.printStackTrace(System.out);
		}
	}
	
	
	public static void main(String[] args) throws Exception {
//		String sourceFolderStr = "c://users/nfrei/desktop/data/language-detection";
		String sourceFolderStr = "c://users/nfrei/desktop/data/language-detection/text-export.zip";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		File outFolder=new File(sourceFolderStr.endsWith(".zip") ? sourceFolder.getParentFile() : sourceFolder, "samples");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptGenerateTextStats processor=new ScriptGenerateTextStats(sourceFolder, outFolder);
		processor.process();
		processor.saveResult();		
		
	}
}
