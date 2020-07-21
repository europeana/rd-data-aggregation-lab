package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RiotException;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.StatisticCalcMean;
import inescid.util.datastruct.HashMapWithFactory;
import inescid.util.datastruct.MapOfInts;

public class TestEdmAlignmentOnEntityCollectionUris {
	private static Pattern hostnamePattern=Pattern.compile("^https?://([^/]+)"); 
	
	public static class ResultsOfTest {
		Map<String, StatisticCalcMean> stmsInEdm=new HashMap<String, StatisticCalcMean>();
		Map<String, StatisticCalcMean> stmsInSource=new HashMap<String, StatisticCalcMean>();
		
		MapOfInts<String> sourceCounts=new MapOfInts<String>();
		MapOfInts<String> sourceCountsErrors=new MapOfInts<String>();
		MapOfInts<String> unconvertableToEdm=new MapOfInts<String>();
		
		Map<String, UsageProfiler> profileOfEdm=new HashMapWithFactory<String, UsageProfiler>(UsageProfiler.class);
		Map<String, UsageProfiler> profileOfSource=new HashMapWithFactory<String, UsageProfiler>(UsageProfiler.class);
		Map<String, UsageProfiler> profileOfSourceUnconvertable=new HashMapWithFactory<String, UsageProfiler>(UsageProfiler.class);
		
		public void process(String hostname, Resource source, Resource convertedToEdm) {
			sourceCounts.incrementTo(hostname);
			if(convertedToEdm==null) {
				unconvertableToEdm.incrementTo(hostname);
				profileOfSourceUnconvertable.get(hostname).collect(source);
			} else {
				profileOfSource.get(hostname).collect(source);
				profileOfEdm.get(hostname).collect(convertedToEdm);				
				stmsInEdm.get(hostname).enter(convertedToEdm.listProperties().toList().size());
				stmsInSource.get(hostname).enter(source.listProperties().toList().size());
			}
		}
		public void processError(String hostname, String uri) {
			sourceCountsErrors.incrementTo(hostname);
		}
		
		public void print() {
			for(String hostname: sourceCounts.keySet()) {
				System.out.println("Hostname: "+hostname+
						" - "+sourceCounts.get(hostname)+
				" ; errors: "+sourceCountsErrors.get(hostname)+
				" ; unconvertable: "+unconvertableToEdm.get(hostname));
			}
			for(String hostname: stmsInEdm.keySet()) {
				System.out.println("Hostname: "+hostname);
				StatisticCalcMean stmsEdmStats = stmsInEdm.get(hostname);
				StatisticCalcMean stmsSourceStats = stmsInSource.get(hostname);
				System.out.println("Avg. statements(SourceEDM): "+stmsSourceStats.getMean());
				System.out.println("Avg. statements(EDM): "+stmsEdmStats.getMean());
			}
			
		}
	}
	
	
	
	public static void main(String[] args) throws Exception {
		String outputFolder = "c://users/nfrei/desktop/data/";

		if (args != null) {
			if (args.length >= 1) {
				outputFolder = args[0];
			}
		}

		Global.init_componentHttpRequestService();
		//	Global.init_componentDataRepository(repoFolder);
		//	Global.init_enableComponentHttpRequestCache();
		//	Repository repository = Global.getDataRepository();
		
		File mapsFile = new File(outputFolder, "context_uris.mvstore.bin");
		if (!mapsFile.exists()) {
			System.out.println("mvStore not found "+mapsFile.getAbsolutePath());
			System.exit(0);
		}
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();
		
		MVMap<String, String> urisConcept = mvStore.openMap("Concept");
		MVMap<String, String> urisPlace = mvStore.openMap("Place");
		MVMap<String, String> urisAgent = mvStore.openMap("Agent");
		MVMap<String, String> urisTimespan = mvStore.openMap("Timespan");

		MVMap<String, String> urisResolvable = mvStore.openMap("ResolvableUris");
		
		Alignment edmAlignment=AlignmentEdmSemanticWeb.create();
		Converter edmConverter=new Converter(edmAlignment);
		
		ResultsOfTest result=new ResultsOfTest();
		
		MapOfInts<String> hostnameCounts=new MapOfInts<String>();
		int maxPerHost=5;
		int urisProcessed=0;
		
		for(String uri: urisConcept.keySet()) {
			if(!urisResolvable.containsKey(uri))
				continue;
			
			Matcher matcher = hostnamePattern.matcher(uri);
			if(matcher.find()) {
				String hostname=matcher.group(1);
				int countForHost=hostnameCounts.incrementTo(hostname);
				if(countForHost>=maxPerHost)
					continue;
				
				try {
					Resource uriRes = RdfUtil.readRdfResourceFromUri(uri);
					if(uriRes!=null) {
						Resource converted = edmConverter.convert(uriRes);
						result.process(hostname, uriRes, converted);
					}
					urisProcessed++;
				} catch (Exception e) {
					e.printStackTrace();
					result.processError(hostname, uri);		
				}
			} else {
				System.out.println("URI unparsable: "+uri);
			}
			if(urisProcessed>0 && urisProcessed %10 ==0)
				System.out.println("Processed "+urisProcessed);
			if(urisProcessed >= 100) {
				System.out.println("Stop for test inspection  "+urisProcessed);
				break;
			}
		}

		result.print();
		
	}
}
