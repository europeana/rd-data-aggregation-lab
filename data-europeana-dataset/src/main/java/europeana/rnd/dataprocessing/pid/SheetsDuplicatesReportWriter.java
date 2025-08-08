package europeana.rnd.dataprocessing.pid;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import europeana.rnd.dataprocessing.ExampleInResource;
import europeana.rnd.dataprocessing.pid.ScriptCheckDuplicatePids.RepeatedPidStats;
import inescid.util.AccessException;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.datastruct.MapOfInts.Sort;

public class SheetsDuplicatesReportWriter {
//	HashMap<PidType, SheetsPrinter> examplesPrinters;
	String spreadsheetId;
	
	public SheetsDuplicatesReportWriter(String spreadsheetId) {
		this.spreadsheetId = spreadsheetId;
	}

	public void write(MapOfLists<String, String> pidToDuplicateChos, RepeatedPidStats stats) throws IOException {
		SheetsPrinter printer = new SheetsPrinter(spreadsheetId, "Repeated PIDs");
		printer.printRecord("PID Scheme", "Count", "Examples");
		for(Entry<PidType, Integer> pidStat: stats.byType.getSortedEntries(Sort.BY_KEY_ASCENDING)) {
			PidType pidType = pidStat.getKey();
			printer.print(pidType.toString(), pidStat.getValue());
			for(ExampleInResource e: stats.examples.get(pidType).getSample())
				printer.print(e.getUri());
			printer.println();
		}		
		
		printer.println();
		printer.printRecord("Repetitions", "PID Count", "Examples");
		for(Entry<Integer, Integer> repetitionsStat: stats.repetitionsStats.getSortedEntries(Sort.BY_KEY_ASCENDING)) {
			Integer repCount = repetitionsStat.getKey();
			printer.print(repCount.toString(), repetitionsStat.getValue());
			for(ExampleInResource e: stats.repetitionExamples.get(repCount).getSample())
				printer.print(e.getUri());
			printer.println();
		}		
		printer.println();
		
		printer.close();
	}
}
