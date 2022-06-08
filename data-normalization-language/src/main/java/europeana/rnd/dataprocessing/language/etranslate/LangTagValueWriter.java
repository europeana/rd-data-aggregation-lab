package europeana.rnd.dataprocessing.language.etranslate;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import europeana.rnd.dataprocessing.language.Source;

public class LangTagValueWriter {
	File outputFolder; 
	
	HashMap<String, FileWriterWithEncoding> writers=new HashMap<String, FileWriterWithEncoding>();
	HashMap<String, CSVPrinter> csvWriters=new HashMap<String, CSVPrinter>();
	
	public LangTagValueWriter(String outputFolder) {
		this.outputFolder=new File(outputFolder);
	}

	public void write(Source source, String resourceUri, String property, String langTag, String text) throws IOException {
		CSVPrinter wrt=getWriter(source, langTag);
		wrt.printRecord(resourceUri, property, text);
	}
	
	private CSVPrinter getWriter(Source source, String langTag) throws IOException {
		CSVPrinter printer=csvWriters.get(source.name()+langTag); 
		if(printer==null) {
			FileWriterWithEncoding wrt = new FileWriterWithEncoding(new File(outputFolder, source.name()+"_"+ URLEncoder.encode(langTag, "UTF-8")+".csv"), StandardCharsets.UTF_8);
			writers.put(source.name()+langTag, wrt);
			printer=new CSVPrinter(wrt, CSVFormat.DEFAULT);
			csvWriters.put(source.name()+langTag, printer);
		}
		return printer;
	}
	
	public void close() throws IOException {
		for(CSVPrinter wrt: csvWriters.values())
			wrt.close();
		for(FileWriterWithEncoding wrt: writers.values())
			wrt.close();
	}
}
