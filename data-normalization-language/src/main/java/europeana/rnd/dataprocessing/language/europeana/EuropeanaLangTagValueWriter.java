package europeana.rnd.dataprocessing.language.europeana;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.output.FileWriterWithEncoding;

import europeana.rnd.dataprocessing.language.Source;

public class EuropeanaLangTagValueWriter {
	
	public static class LangTagValue {
		public String value;
		public String choUri;
		public String subjectUri;
		public String rdfType;
		public String property;
		public String text;
	}
	
	
	File outputFolder; 
	
	HashMap<String, FileWriterWithEncoding> writers=new HashMap<String, FileWriterWithEncoding>();
	HashMap<String, CSVPrinter> csvWriters=new HashMap<String, CSVPrinter>();
	
	public EuropeanaLangTagValueWriter(String outputFolder) {
		this.outputFolder=new File(outputFolder);
		if(!this.outputFolder.exists())
			this.outputFolder.mkdir();
	}

	public void write(String choUri, Source source, String subjectUri, String rdfType, String property, String langTag, String text) throws IOException {
		CSVPrinter wrt=getWriter(source, langTag);
		wrt.printRecord(choUri, subjectUri, rdfType, property, text);
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
	
	public static LangTagValue read(CSVRecord rec) {
		LangTagValue val=new LangTagValue();
		val.choUri=rec.get(0);
		val.subjectUri=rec.get(1);
		val.rdfType=rec.get(2);
		val.property=rec.get(3);
		val.text=rec.get(4);
		return val;
	}
}
