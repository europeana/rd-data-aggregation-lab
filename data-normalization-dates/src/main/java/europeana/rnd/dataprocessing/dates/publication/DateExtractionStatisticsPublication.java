package europeana.rnd.dataprocessing.dates.publication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import europeana.rnd.dataprocessing.dates.CleanId;
import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.dataprocessing.dates.DatesInRecord.DateValue;
import europeana.rnd.dataprocessing.dates.Source;
import europeana.rnd.dataprocessing.dates.stats.Examples;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfInts.Sort;
import inescid.util.datastruct.MapOfMaps;
import inescid.util.datastruct.MapOfMapsOfInts;
import inescid.util.datastruct.MapOfSets;

public class DateExtractionStatisticsPublication {
	MapOfMaps<String, PatternId, Examples> statsByClassProvider;
	MapOfMaps<String, PatternId, Examples> statsByClassAndPropertyProvider;
	MapOfMaps<String, PatternId, Examples> statsByClassEuropeana;
	MapOfMaps<String, PatternId, Examples> statsByClassAndPropertyEuropeana;
	Map<PatternId, Examples> statsByMatch;
	
//	MapOfMapsOfInts<String, MatchId> statsByProperty;
//	MapOfMapsOfInts<String, MatchId> statsByClass;
//	HashMap<String, MapOfMapsOfInts<String, MatchId>> statsByClassProperty;
	MapOfInts<PatternId> statsGlobal;
	MapOfInts<CleanId> statsGlobalClean;

	public DateExtractionStatisticsPublication() {
		statsByClassProvider=new MapOfMaps<String, PatternId, Examples>();
		statsByClassAndPropertyProvider=new MapOfMaps<String, PatternId, Examples>();
		statsByClassEuropeana=new MapOfMaps<String, PatternId, Examples>();
		statsByClassAndPropertyEuropeana=new MapOfMaps<String, PatternId, Examples>();
		statsByMatch=new HashMap<PatternId, Examples>();
//		statsByProperty=new MapOfMapsOfInts<String, MatchId>();
//		statsByClass=new MapOfMapsOfInts<String, MatchId>();
//		statsByClassProperty=new HashMap<String, MapOfMapsOfInts<String,MatchId>>();
		statsGlobal=new MapOfInts<PatternId>();
		statsGlobalClean=new MapOfInts<CleanId>();
	}
	
	static final int lengthOfChoUriPrefix = "http://data.europeana.eu/item/".length();
	public void add(String choUri, Source source, DateValue match) {		
		PatternId pattern=PatternId.fromMatch(match.match);
		if(source == Source.PROVIDER) {
			Examples examples = statsByClassProvider.get(match.className, pattern);
			if(examples==null) {
				examples=new Examples();
				statsByClassProvider.put(match.className, pattern, examples);
			}
			examples.add(match.match.getInput());
			
			examples = statsByClassAndPropertyProvider.get(match.className+","+match.property, pattern);
			if(examples==null) {
				examples=new Examples();
				statsByClassAndPropertyProvider.put(match.className+","+match.property, pattern, examples);
			}
			examples.add(match.match.getInput());
		} else {
			Examples examples = statsByClassEuropeana.get(match.className, pattern);
			if(examples==null) {
				examples=new Examples();
				statsByClassEuropeana.put(match.className, pattern, examples);
			}
			examples.add(match.match.getInput());
			
			examples = statsByClassAndPropertyEuropeana.get(match.className+","+match.property, pattern);
			if(examples==null) {
				examples=new Examples();
				statsByClassAndPropertyEuropeana.put(match.className+","+match.property, pattern, examples);
			}
			examples.add(match.match.getInput());
		}
		
		Examples examples = statsByMatch.get(pattern);
		if(examples==null) {
			examples=new Examples(500);
			statsByMatch.put(pattern, examples);
		}
		examples.add(match.match.getInput());
		
		statsGlobal.incrementTo(pattern);
		if(match.match.getCleanOperation()!=null)
			statsGlobalClean.incrementTo(match.match.getCleanOperation());
	}
	
	public void save(File toFolder) throws IOException {
		File globalToFile=new File(toFolder, "stats-global.csv");
		File globalCleanToFile=new File(toFolder, "stats-global-clean.csv");
		File collectionsToFile=new File(toFolder, "stats-collections.csv");
		File matchToFile=new File(toFolder, "stats-matches.csv");
		File classesToFileProvider=new File(toFolder, "stats-class-provider.csv");
		File classesAndPropertiesToFileProvider=new File(toFolder, "stats-class-and-property-provider.csv");
		File classesToFileEuropeana=new File(toFolder, "stats-class-europeana.csv");
		File classesAndPropertiesToFileEuropeana=new File(toFolder, "stats-class-and-property-europeana.csv");
		
		FileWriterWithEncoding writer=new FileWriterWithEncoding(globalToFile, StandardCharsets.UTF_8);
		statsGlobal.writeCsv(writer, Sort.BY_KEY_ASCENDING);
		writer.close();
		
		writer=new FileWriterWithEncoding(globalCleanToFile, StandardCharsets.UTF_8);
		statsGlobalClean.writeCsv(writer, Sort.BY_KEY_ASCENDING);
		writer.close();
		
		writer=new FileWriterWithEncoding(matchToFile, StandardCharsets.UTF_8);
		writeMapCsv(statsByMatch, writer);
		writer.close();
		
		writer=new FileWriterWithEncoding(classesToFileProvider, StandardCharsets.UTF_8);
		writeMapCsv(statsByClassProvider, writer);
		writer.close();
		
		writer=new FileWriterWithEncoding(classesAndPropertiesToFileProvider, StandardCharsets.UTF_8);
		writeMapCsv(statsByClassAndPropertyProvider, writer);
		writer.close();

		writer=new FileWriterWithEncoding(classesToFileEuropeana, StandardCharsets.UTF_8);
		writeMapCsv(statsByClassEuropeana, writer);
		writer.close();
		
		writer=new FileWriterWithEncoding(classesAndPropertiesToFileEuropeana, StandardCharsets.UTF_8);
		writeMapCsv(statsByClassAndPropertyEuropeana, writer);
		writer.close();


	}
	
	private void writeMapCsv(MapOfMaps<String, PatternId, Examples> sets, FileWriterWithEncoding csvWrite) throws IOException {
		CSVPrinter printer=new CSVPrinter(csvWrite, CSVFormat.DEFAULT);
		ArrayList<Entry<String, Map<PatternId, Examples>>> allEntries=new ArrayList(sets.entrySet());
		for(Entry<String, Map<PatternId, Examples>> e1: allEntries) {
			boolean first=true;
			for(Object k2 :  e1.getValue().keySet()) {
				if(first) {
					first=false;
					printer.print(e1.getKey().toString());
				} else
					printer.print("");
				boolean first2=true;
				for(String ex: e1.getValue().get(k2).getSample()) {
					if(first2) {
						first2=false;
						printer.print(k2.toString());
					} else
						printer.print("");
					printer.print(ex);					
				}
				printer.println();
			}
		}
		printer.close();
	}	
	
	private void writeMapCsv(Map<PatternId, Examples> sets, FileWriterWithEncoding csvWrite) throws IOException {
		CSVPrinter printer=new CSVPrinter(csvWrite, CSVFormat.DEFAULT);
		for(Entry<PatternId, Examples>  r: sets.entrySet()) {
			boolean first=true;
			for(String ex: r.getValue().getSample()) {
				if(first) {
					first=false;
					printer.printRecord(r.getKey().toString(), r.getValue().getTotalFound(), ex);
				} else
					printer.printRecord("", "", ex);
			}
		}
		printer.close();
	}

	
	
}
