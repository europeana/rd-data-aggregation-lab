package europeana.rnd.dataprocessing.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

public class ScriptNormalizeLanguageReport {

	File folder;
	
	public ScriptNormalizeLanguageReport(File folder, File outputFolder) {
		super();
		this.folder = folder;
	}

	public void process() throws IOException {
	}


	public static void main(String[] args) throws Exception {
		String sourceFolder = "c://users/nfrei/desktop/data/language";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolder = args[0];
			}
		}
		File outFolder=new File(sourceFolder+"/extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptNormalizeLanguageReport processor=new ScriptNormalizeLanguageReport(new File(sourceFolder), outFolder);
		processor.process();
	}
}
