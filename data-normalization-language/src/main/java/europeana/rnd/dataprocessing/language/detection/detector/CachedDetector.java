package europeana.rnd.dataprocessing.language.detection.detector;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import europeana.rnd.dataprocessing.language.detection.Result;
import europeana.rnd.dataprocessing.language.detection.Result.DetectedLanguage;
import inescid.util.AccessException;

public class CachedDetector extends Detector {
	Detector trueDetector;
	File cacheFolder;
	Map<String, DetectedLanguage> cache;
	int callsCounter=0;
	boolean useOnlyCache;
	
	public CachedDetector(Detector trueDetector, File cacheFolder, boolean useOnlyCache) throws IOException {
		super(trueDetector.getName());
		this.trueDetector = trueDetector;
		this.cacheFolder=cacheFolder;
		this.useOnlyCache = useOnlyCache;
		cache=new HashMap<String, DetectedLanguage>();
		load();
	}

	@Override
	public Result detectLanguage(String text) throws AccessException {
		callsCounter++;
		DetectedLanguage detectedLanguage = cache.get(text);
		Result result = null;
		if(detectedLanguage!=null) {
			result = new Result();
			result.add(detectedLanguage.getLangCode(), detectedLanguage.getConfidence());
		} else if (useOnlyCache){
			return null;
		}else {
			result = trueDetector.detectLanguage(text);
			System.out.println("Requesting uncached from "+trueDetector.getName()+" - "+result);
			cache.put(text, result.getFirstResult());
		}
		try {
			if(!useOnlyCache && callsCounter % 10 == 0)
				save();
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
		return result;
	}

	protected void load() throws IOException {
		File cacheFile=new File(cacheFolder, trueDetector.getName()+ "-cache.csv");
		if(!cacheFile.exists())
			return;
		FileReader fileReader = new FileReader(cacheFile, StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(fileReader, CSVFormat.DEFAULT);
		parser.forEach(rec ->{
			DetectedLanguage firstResult=new DetectedLanguage(rec.get(1), Float.parseFloat(rec.get(2)));
			cache.put(rec.get(0), firstResult);
		});
		parser.close();
		fileReader.close();
	}
	
	public void save() throws IOException {
		File cacheFile=new File(cacheFolder, trueDetector.getName()+ "-cache.csv");
		FileWriter fileWriter = new FileWriter(cacheFile, StandardCharsets.UTF_8);
		CSVPrinter printer=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
		cache.forEach((txt, result) -> {
			try {
				if(result==null)
					printer.printRecord(txt, "", 0);
				else
					printer.printRecord(txt, result.getLangCode(), result.getConfidence());
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
		printer.close();
		fileWriter.close();
	}
	
	
}
