package europeana.rnd.dataprocessing.uri;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import europeana.rnd.dataprocessing.uri.UrisInRecord.UrisInProperty;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;


public class ScriptExportEntityUrisToTxt {
	File folder;
	File outputFolder;
	
	HashSet<Property> selectedProperties=new HashSet<Property>() {{ 
		add(Dc.subject);
		add(Dc.creator);
		add(Dc.contributor);
		add(Dc.coverage);
		add(Dc.format);
		add(DcTerms.spatial);
		add(DcTerms.temporal);
	}};
	
	
	public ScriptExportEntityUrisToTxt(File folder, File outputFolder) {
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
				if(!innerFolder.getName().startsWith("language_export_")) continue;
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
	
	private void processFile(InputStream is) {
		JsonParser parser = Json.createParser(is);
		parser.next();
		Stream<JsonValue> arrayStream = parser.getArrayStream();
		for(Iterator<JsonValue> it=arrayStream.iterator() ; it.hasNext() ;) {
			JsonObject jv=it.next().asJsonObject();
			UrisInRecord rec=new UrisInRecord(jv);
			try {
				Set<Entry<Resource, UrisInProperty>> entries = rec.fromProvider.urisByClass.entrySet();
				for(Entry<Resource, UrisInProperty> clsEntry: entries) {
					export(clsEntry.getKey().getLocalName(), clsEntry.getValue());
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	private void export(String className, UrisInProperty urisInProperty ) {
		for(String uri: urisInProperty.getAllUrisInProperties(selectedProperties)) {
			if(! uri.startsWith("http"))
				return;
			try {
				FileUtils.write(new File(outputFolder, className+"-uris.txt"), urisInProperty+"\n", StandardCharsets.UTF_8, true);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		String sourceFolderStr = "c://users/nfrei/desktop/data/uri/uris_export.zip";

		if (args != null) {
			if (args.length >= 1) {
				sourceFolderStr = args[0];
			}
		}
		File sourceFolder = new File(sourceFolderStr);
		File outFolder=new File(sourceFolderStr.endsWith(".zip") ? sourceFolder.getParentFile() : sourceFolder, "extraction");
		if(!outFolder.exists()) 
			outFolder.mkdir();
		
		ScriptExportEntityUrisToTxt processor=new ScriptExportEntityUrisToTxt(sourceFolder, outFolder);
		processor.process();
	}
}
