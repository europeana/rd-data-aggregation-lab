package europeana.rnd.dataprocessing.language.detection.evaluation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.ibm.icu.text.Edits.Iterator;

public class TestSet {

	public enum TextLength {
		LONG, SHORT
	}
	public class TextValue {
		String text;
		String property;
		String lang;
		
		public TextValue(String text, String property, String lang) {
			super();
			this.text = text;
			this.property = property;
			this.lang = lang;
		}
	}
	
	File folder;
	
	public TestSet(File folder) {
		this.folder = folder;
	}

	public Set<String> getAvailableLanguages() {
		HashSet<String> langs=new HashSet<String>();
		for(File f: folder.listFiles()) {
			if(f.isDirectory()) continue;
			if(f.getName().endsWith("-short.txt") || f.getName().endsWith("-long.txt")) {
				langs.add(f.getName().substring(0,2).toLowerCase());
			}
		}
		return langs;
	}

	public List<TextValue> listValues(String lang, TextLength length, int maxValues) throws IOException {
		File samplesFile=new File(folder, lang.toLowerCase()+"-"+length.name().toLowerCase()+".txt");
		
		FileReader fileReader=new FileReader(samplesFile);
		BufferedReader reader=new BufferedReader(fileReader);
		
		List<TextValue> values=new ArrayList<TestSet.TextValue>(maxValues); 
		CSVParser csv=new CSVParser(fileReader, CSVFormat.DEFAULT);
		for(CSVRecord rec: csv) {
			values.add(new TextValue(rec.get(0), rec.get(1), lang.toLowerCase()));
			if(maxValues>0 && values.size()>=maxValues)
				break;
//		for(java.util.Iterator<String> it=reader.lines().iterator() ; it.hasNext() && (maxValues<=0 || maxValues>values.size()); ) {
		}
		return values;
	}
	
	
}
