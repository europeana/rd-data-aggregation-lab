package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.ThreadedRunner;
import inescid.util.datastruct.MapOfInts;

public class ScriptCheckResolvableUris {
	private static final int MAX_FAILS=100;

	static HashSet<String> unknownHosts=new HashSet<String>();
	static String lastUriChecked=null;

	static File resumptionFile = null;
	static File unknownHostsFile= null;
	
	static int resolvableUrisCount=0;
	static int unResolvableUrisCount=0;
	
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
		
		resumptionFile = new File(outputFolder, "context_uris_check.resumption.txt");
		if(resumptionFile.exists()) {
			lastUriChecked=FileUtils.readFileToString(resumptionFile, StandardCharsets.UTF_8);
			System.out.println("Resuming at URI: "+lastUriChecked);
		}
		unknownHostsFile = new File(outputFolder, "context_uris_check.unknown_hosts.txt");
		if(unknownHostsFile.exists())
			unknownHosts=new HashSet<String>(FileUtils.readLines(unknownHostsFile, StandardCharsets.UTF_8));
	
		File mapsFile = new File(outputFolder, "context_uris.mvstore.bin");
		if (!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();

		MVMap<String, String> urisConcept = mvStore.openMap("Concept");
		MVMap<String, String> urisPlace = mvStore.openMap("Place");
		MVMap<String, String> urisAgent = mvStore.openMap("Agent");
		MVMap<String, String> urisTimespan = mvStore.openMap("Timespan");

		MVMap<String, String> urisResolvable = mvStore.openMap("ResolvableUris");

		if(!resumptionFile.exists())
			urisResolvable.clear();
		MVStore.TxCounter txCounter = mvStore.registerVersionUsage();
	     try {
			runTest(urisConcept, urisResolvable);
			runTest(urisPlace, urisResolvable);
			runTest(urisTimespan, urisResolvable);
			runTest(urisAgent, urisResolvable);
	     } finally {
	    	 mvStore.deregisterVersionUsage(txCounter);
	     }
		
		mvStore.close();
		Global.shutdown();		
		System.out.println("FININSHED TEST OF URIS");
	}


	private static void runTest(MVMap<String, String> uris, MVMap<String, String> urisResolvable) throws Exception {
		final Pattern hostPattern=Pattern.compile("^https?://([^/]+)/");
		
		ThreadedRunner runner=new ThreadedRunner(10, 15);  
		
		int cnt=0;
		for(Iterator<String> it = uris.keyIterator(null) ; it.hasNext() ; ) {
			//for debug
//			if ((resolvableUrisCount ) > 500) 
//				break;
			String uri=it.next();
			if(lastUriChecked!=null) {
				if(uri.equals(lastUriChecked))
					lastUriChecked=null;
				continue;
			}
			try {
				new URI(uri);
			} catch (Exception e) {
				continue;
			}
			if(cnt !=0 && cnt % 100 == 0) {
				System.out.println("Processed: "+cnt);
			}
				
			Matcher matcher = hostPattern.matcher(uri);
			if(matcher.find()) {
				String host=matcher.group(1);
				if(unknownHosts.contains(host) || host.matches("^\\d+.*")) 
					continue;
				cnt++;
				FileUtils.write(resumptionFile, uri, StandardCharsets.UTF_8, false);
				
				runner.run(new Runnable() {
					@Override
					public void run() {
//						System.out.println(uri);
						try {
							if(RdfUtil.isUriResolvable(uri)) {
								urisResolvable.put(uri, "");
								resolvableUrisCount++;
							} else
								unResolvableUrisCount++;
						} catch (AccessException e) {
							if(e.getCause()!=null && e.getCause().getClass().equals(UnknownHostException.class)) {
								unknownHosts.add(host);
								try {
									FileUtils.writeLines(unknownHostsFile, "UTF-8", unknownHosts);
								} catch (IOException e1) {
									System.err.println("Unexpected exception");
									e1.printStackTrace();
								}
							} else {
								unResolvableUrisCount++;
								e.printStackTrace();
							}
						} catch (InterruptedException | IOException e) {
							e.printStackTrace();
							unResolvableUrisCount++;
						}
						
						if ((resolvableUrisCount + unResolvableUrisCount) % 100 == 0) {
							System.out.println("Processed: resolvable-"+resolvableUrisCount+" ; unresolvable-"+unResolvableUrisCount );	
						}
					}
				});
			}
		}
		runner.awaitTermination(10);	
		
//		System.out.println("\nFINAL RESULTS:");
//
//		for(String host: new ArrayList<String>(withData)) {
//			System.out.println(host);
//		}
//		System.out.println("\nWITHOUT DATA:");
//		for(String host: new ArrayList<String>(withoutData)) {
//			System.out.println(host);
//		}
//		for(String host: checks.keySet()) {
//			System.out.println(host);
//		}
	}
	

	
}
