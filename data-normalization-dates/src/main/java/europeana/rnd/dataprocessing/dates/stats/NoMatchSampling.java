package europeana.rnd.dataprocessing.dates.stats;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import europeana.rnd.dataprocessing.dates.CleanId;
import europeana.rnd.dataprocessing.dates.Match;
import europeana.rnd.dataprocessing.dates.MatchId;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfMapsOfInts;
import inescid.util.datastruct.MapOfSets;

public class NoMatchSampling {
	MapOfInts<String> noMatchStatsGlobal;
	MapOfSets<String,String> noMatchExamples;

	public NoMatchSampling() {
		noMatchStatsGlobal=new MapOfInts<String>();
		noMatchExamples=new MapOfSets<String,String>();
	}
	
	public void add(Match match) {
		String key=createKey(match.getInput());
		noMatchStatsGlobal.incrementTo(key);
		if(noMatchExamples.containsKey(key)) {
			if(noMatchExamples.get(key).size()<20)
				noMatchExamples.put(key, match.getInput());			
		} else
			noMatchExamples.put(key, match.getInput());
	}
		
	private String createKey(String input) {
		return input.replaceAll("\\d", "#")
				.replaceAll("\\n", " ")
				.replaceAll("[\\p{IsLatin}\\p{InGreek}]", "W")
				.replaceAll("[W\\s]{10,}", "WWWWWWWWWW");
	}

	public void save(File globalToFile) throws IOException {
		FileWriterWithEncoding csvWrite=new FileWriterWithEncoding(globalToFile, StandardCharsets.UTF_8);
		CSVPrinter printer=new CSVPrinter(csvWrite, CSVFormat.DEFAULT);
		for(String  key: noMatchStatsGlobal.getSortedKeysByInts()) {
			printer.print(key);
			printer.print(noMatchStatsGlobal.get(key).toString());
			for(String example: noMatchExamples.get(key)) 
				printer.print(example.replaceAll("\\n", " "));
			printer.println();
		}
		printer.close();
		csvWrite.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		NoMatchSampling s=new NoMatchSampling();
		s.add(new Match("1234,:(nuno"));
		s.add(new Match("1454,:(jose"));
		s.add(new Match("14x?,:(jose"));
		s.save(new File("target/text.csv"));
		
	}
	
}
