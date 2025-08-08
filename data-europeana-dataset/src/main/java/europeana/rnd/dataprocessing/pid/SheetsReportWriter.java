package europeana.rnd.dataprocessing.pid;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import europeana.rnd.dataprocessing.MapOfIntsWithExamples;
import europeana.rnd.dataprocessing.ExampleInResource;
import inescid.util.AccessException;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.datastruct.MapOfInts.Sort;
import inescid.util.europeana.EdmRdfUtil;
import inescid.util.europeana.EdmXmlUtil;

public class SheetsReportWriter {
//	HashMap<PidType, SheetsPrinter> examplesPrinters;
	String spreadsheetId;
	
	public SheetsReportWriter(String spreadsheetId) {
		this.spreadsheetId = spreadsheetId;
	}

	public void write(IdStats stats) throws IOException {
		SheetsPrinter printer = new SheetsPrinter(spreadsheetId, "General stats");
		printer.printRecord("PID Scheme", "Count", "Count Unique");
		for(Entry<PidType, Integer> pidStat: stats.statsByType.getSortedEntries(Sort.BY_KEY_ASCENDING)) {
			printer.printRecord(pidStat.getKey().toString(), pidStat.getValue(), stats.statsByTypeUnique.get(pidStat.getKey()));
		}
		printer.println();
		printer.printRecord("Records with 1+ PIDs", "Records without PID");
		printer.printRecord(stats.countUnique, stats.countWithoutPid);

		printer.println();
		printer.printRecord("Property with PIDs", "Count");
		for(Entry<String, Integer> fieldStat: stats.getStatsByField().getSortedEntries(Sort.BY_KEY_ASCENDING)) {
			String field = fieldStat.getKey();
			printer.printRecord(field, fieldStat.getValue());
		}
		
		printer.println();
		printer.printRecord("Multiple PIDs in a single record");				
		printer.printRecord("PID Types", "Count", "Examples");				
		for(Entry<String, Integer> typeEntry: stats.multiplePidStats.byType.getSortedEntries(Sort.BY_KEY_ASCENDING)) {
			String type=typeEntry.getKey();
			printer.print(type, typeEntry.getValue());
			for(ExampleInResource e: stats.multiplePidStats.examples.get(type).getSample())
				printer.print("=HYPERLINK(\""+getRecorApiUrl(e.getUri())+"\",\""+e.getValue()+"\")"  );
			printer.println();
		}
		printer.close();
		for(PidType type: PidType.values()) {
			printer = new SheetsPrinter(spreadsheetId, "Stats "+type);
			Integer cnt=stats.statsByType.get(type);
			if(cnt==null) 
				cnt=0;
//			printer.printRecord(type.toString(), cnt);
			if(cnt!=0) {
				printer.printRecord("Breakdown by property");
				printer.printRecord("Property", "Count", "Examples");
				MapOfInts<String> statsByField = stats.statsByTypeAndField.get(type);
				List<Entry<String, Integer>> sortedEntries = statsByField.getSortedEntries(Sort.BY_KEY_ASCENDING);
				for(Entry<String, Integer> fieldStat: sortedEntries) {
					String field = fieldStat.getKey();
					printer.print(field, fieldStat.getValue());
					for(ExampleInResource e: stats.examples.get(type, field).getSample())
						printer.print("=HYPERLINK(\""+getRecorApiUrl(e.getUri())+"\",\""+e.getValue()+"\")"  );
					printer.println();
				}
				printer.println();
				
				MapOfIntsWithExamples<String, String> statsByDomain = stats.getStatsByDomain(type);
				if(statsByDomain!=null) {
					printer.printRecord("Breakdown by authority");
					printer.printRecord("Authority", "Count");
					sortedEntries = statsByDomain.getSortedEntries();
					for(Entry<String, Integer> fieldStat: sortedEntries) {
						String field = fieldStat.getKey();
						printer.print(field, fieldStat.getValue());
						Set<String> sample = statsByDomain.getSample(field);
						for(String exChoUri: sample)
							printer.print("=HYPERLINK(\""+getRecorApiUrl(exChoUri) +"\",\""+exChoUri+"\")"  );
						printer.println();
					}
				}
			} else
				printer.printRecord("No cases were found");
			printer.close();
		}
		{
			printer = new SheetsPrinter(spreadsheetId, "Stats by provider");

			HashSet<String> providers=new HashSet(stats.countWithoutPidByProvider.keySet());
			providers.addAll(stats.countWithPidByProviderAndType.keySet());
			printer.printRecord("Records with(out) PID by data provider");
			printer.printRecord("Data provider", "Without PID", "With PID", "With ARK", "With HANDLE", "With DOI", "With URN", "With Purl", "Provider");
			for (String dataProv : providers) {
				if(stats.countWithPidByProviderAndType.containsKey(dataProv)) {
					printer.print(dataProv, 
							stats.countWithoutPidByProvider.get(dataProv),
							stats.countWithPidByProviderAndType.get(dataProv).sizeTotal(),
							stats.countWithPidByProviderAndType.get(dataProv, PidType.ARK),
							stats.countWithPidByProviderAndType.get(dataProv, PidType.HANDLE),
							stats.countWithPidByProviderAndType.get(dataProv, PidType.DOI),
							stats.countWithPidByProviderAndType.get(dataProv, PidType.URN),
							stats.countWithPidByProviderAndType.get(dataProv, PidType.PURL)
							);
				} else
					printer.print(dataProv, stats.countWithoutPidByProvider.get(dataProv), 0,0,0,0,0,0	);
				if(stats.providersOfDataProvider.containsKey(dataProv)) {
					for(String provider: stats.providersOfDataProvider.get(dataProv))
						printer.print(provider);
				} else 
					printer.print("(without provider)");
				printer.println();
			}
			printer.close();
		}		
	}

	public void write(PIdSchemeStats stats) throws IOException {
	  SheetsPrinter printer = new SheetsPrinter(spreadsheetId, "General stats");
	  printer.printRecord("PID Scheme", "Count", "Count Unique");
	  for(Entry<PidScheme, Integer> pidStat: stats.statsByType.getSortedEntries(Sort.BY_KEY_ASCENDING)) {
	    printer.printRecord(pidStat.getKey().getSchemeId(), pidStat.getValue(), stats.statsByTypeUnique.get(pidStat.getKey()));
	  }
	  printer.println();
	  printer.printRecord("Records with 1+ PIDs", "Records without PID");
	  printer.printRecord(stats.countUnique, stats.countWithoutPid);
	  
	  printer.println();
	  printer.printRecord("Property with PIDs", "Count");
	  for(Entry<String, Integer> fieldStat: stats.getStatsByField().getSortedEntries(Sort.BY_KEY_ASCENDING)) {
	    String field = fieldStat.getKey();
	    printer.printRecord(field, fieldStat.getValue());
	  }
	  
//	  printer.println();
//	  printer.printRecord("Multiple PIDs in a single record");				
//	  printer.printRecord("PID Types", "Count", "Examples");				
//	  for(Entry<String, Integer> typeEntry: stats.multiplePidStats.byType.getSortedEntries(Sort.BY_KEY_ASCENDING)) {
//	    String type=typeEntry.getKey();
//	    printer.print(type, typeEntry.getValue());
//	    for(ExampleInResource e: stats.multiplePidStats.examples.get(type).getSample())
//	      printer.print("=HYPERLINK(\""+getRecorApiUrl(e.getUri())+"\",\""+e.getValue()+"\")"  );
//	    printer.println();
//	  }
	  printer.close();
	  for(PidScheme type: PidSchemes.allSchemes) {
	    System.out.println("printing scheme "+type.getSchemeId());
	    printer = new SheetsPrinter(spreadsheetId, "Stats "+type.getSchemeId());
	    Integer cnt=stats.statsByType.get(type);
	    if(cnt==null) 
	      cnt=0;
//			printer.printRecord(type.toString(), cnt);
	    if(cnt!=0) {
	      printer.printRecord("Breakdown by property");
	      printer.printRecord("Property", "Count", "Examples");
	      MapOfInts<String> statsByField = stats.statsByTypeAndField.get(type);
	      List<Entry<String, Integer>> sortedEntries = statsByField.getSortedEntries(Sort.BY_KEY_ASCENDING);
	      for(Entry<String, Integer> fieldStat: sortedEntries) {
	        String field = fieldStat.getKey();
	        printer.print(field, fieldStat.getValue());
	        for(ExampleInResource e: stats.examples.get(type, field).getSample())
	          printer.print("=HYPERLINK(\""+getRecorApiUrl(e.getUri())+"\",\""+e.getValue()+"\")"  );
	        printer.println();
	      }
	      printer.println();
	      
	      MapOfIntsWithExamples<String, String> statsByDomain = stats.getStatsByDomain(type);
	      if(statsByDomain!=null) {
	        printer.printRecord("Breakdown by authority");
	        printer.printRecord("Authority", "Count");
	        sortedEntries = statsByDomain.getSortedEntries();
	        for(Entry<String, Integer> fieldStat: sortedEntries) {
	          String field = fieldStat.getKey();
	          printer.print(field, fieldStat.getValue());
	          Set<String> sample = statsByDomain.getSample(field);
	          for(String exChoUri: sample)
	            printer.print("=HYPERLINK(\""+getRecorApiUrl(exChoUri) +"\",\""+exChoUri+"\")"  );
	          printer.println();
	        }
	      }
	    } else
	      printer.printRecord("No cases were found");
	    printer.close();
	  }
//	  {
//	    printer = new SheetsPrinter(spreadsheetId, "Stats by provider");
//	    
//	    HashSet<String> providers=new HashSet(stats.countWithoutPidByProvider.keySet());
//	    providers.addAll(stats.countWithPidByProviderAndType.keySet());
//	    printer.printRecord("Records with(out) PID by data provider");
//	    printer.printRecord("Data provider", "Without PID", "With PID", "With ARK", "With HANDLE", "With DOI", "With URN", "With Purl", "Provider");
//	    for (String dataProv : providers) {
//	      if(stats.countWithPidByProviderAndType.containsKey(dataProv)) {
//	        printer.print(dataProv, 
//	            stats.countWithoutPidByProvider.get(dataProv),
//	            stats.countWithPidByProviderAndType.get(dataProv).sizeTotal(),
//	            stats.countWithPidByProviderAndType.get(dataProv, PidType.ARK),
//	            stats.countWithPidByProviderAndType.get(dataProv, PidType.HANDLE),
//	            stats.countWithPidByProviderAndType.get(dataProv, PidType.DOI),
//	            stats.countWithPidByProviderAndType.get(dataProv, PidType.URN),
//	            stats.countWithPidByProviderAndType.get(dataProv, PidType.PURL)
//	            );
//	      } else
//	        printer.print(dataProv, stats.countWithoutPidByProvider.get(dataProv), 0,0,0,0,0,0	);
//	      if(stats.providersOfDataProvider.containsKey(dataProv)) {
//	        for(String provider: stats.providersOfDataProvider.get(dataProv))
//	          printer.print(provider);
//	      } else 
//	        printer.print("(without provider)");
//	      printer.println();
//	    }
//	    printer.close();
//	  }		
	}

	private String getRecorApiUrl(String choUri) {
		String id=EdmRdfUtil.getApiIdFromRecordUriOrId(choUri);
		return "https://api.europeana.eu/record/v2/"+id+".jsonld?wskey=api2demo";
	}

	public void writeReliability(StatsReliability stats) throws IOException {
		SheetsPrinter printer = new SheetsPrinter(spreadsheetId, "Validation stats");
		printer.printRecord("PID Scheme", "Problem", "Count");
		for(Entry<PidType, Integer> pidStat: stats.invalidByType.getSortedEntries()) {
			printer.printRecord(pidStat.getKey().toString(), "Invalid", pidStat.getValue());
		}
		for(Entry<PidType, Integer> pidStat: stats.unresolvableByType.getSortedEntries()) {
			printer.printRecord(pidStat.getKey().toString(), "Not resolvable", pidStat.getValue());
		}
		for(Entry<PidType, Integer> pidStat: stats.unknownPersistencePolicyByType.getSortedEntries()) {
			printer.printRecord(pidStat.getKey().toString(), "Unknown Persistence Policy", pidStat.getValue());
		}
		for(Entry<PidType, Integer> pidStat: stats.locationDependentByType.getSortedEntries()) {
			printer.printRecord(pidStat.getKey().toString(), "Location dependent", pidStat.getValue());
		}
		
		printer.println();
		printer.printRecord("Records with 1+ valid PIDs", "Records without a valid PID");
		printer.printRecord(stats.countUnique, stats.countWithoutPid);

		printer.println();
		printer.printRecord("Records with valid PIDs by type");
		printer.printRecord("PID Scheme", "Count");
		for(Entry<PidType, Integer> pidStat: stats.okByType.getSortedEntries()) {
			printer.printRecord(pidStat.getKey().toString(), pidStat.getValue());
		}
		
		printer.println();
		printer.printRecord("Multiple PIDs in a single record");				
		printer.printRecord("PID Types", "Count", "Examples");				
		for(Entry<String, Integer> typeEntry: stats.multiplePidStats.byType.getSortedEntries(Sort.BY_KEY_ASCENDING)) {
			String type=typeEntry.getKey();
			printer.print(type, typeEntry.getValue());
			for(ExampleInResource e: stats.multiplePidStats.examples.get(type).getSample())
				printer.print("=HYPERLINK(\""+getRecorApiUrl(e.getUri())+"\",\""+e.getValue()+"\")"  );
			printer.println();
		}
		printer.close();
	}
}
