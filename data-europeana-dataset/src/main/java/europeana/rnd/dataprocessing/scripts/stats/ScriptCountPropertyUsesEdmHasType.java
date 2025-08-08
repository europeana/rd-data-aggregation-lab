package europeana.rnd.dataprocessing.scripts.stats;

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import apiclient.google.GoogleApi;
import europeana.rnd.aggregated.FtpDownload;
import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.aggregated.RecordIterator.Handler;
import europeana.rnd.aggregated.RecordIterator.Handler.ProceedOption;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptCountPropertyUsesEdmHasType {
	public static void main(String[] args) throws Exception {
		String inputFolder = null;
		
		if (args != null && args.length >= 2) {
			inputFolder = args[0];
		}else {
			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset_debug";
		}

		RecordIterator repository=new RecordIterator(new File(inputFolder));
		repository.setLang(Lang.RDFXML);
		
		// INIT OPERATIONS
//		int MAX_EXPORTED=1000; 
		int MAX_EXPORTED=-1; 
		final int[] usesCounter=new int[] {0};
//		String propertyToSearch="dc:format";
//		String propertyToSearch="dc:title";
		String propertyToSearch="edm:hasType";
		
		String searchString="<"+propertyToSearch;
		// INIT OPERATIONS - END		
		try {
			repository.iterateRdfString(new Handler<String, String>() {
				Date start = new Date();
				int okRecs = 0;
				
				public ProceedOption handle(String edmRdfString, String dataset, String recLocalId) {
					if (repository.getCurrentRecordIndex()!=0 && (repository.getCurrentRecordIndex() % 10000 == 0 || repository.getCurrentRecordIndex() == 10)) {
//						csvOut.flush();
//						csvBuffer.flush();
						Date now = new Date();
						long elapsedPerRecord = (now.getTime() - start.getTime()) / repository.getCurrentRecordIndex();
						double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);						
						double hoursToEnd = (double)(RecordIterator.EUROPEANA_DATASET_SIZE - repository.getCurrentRecordIndex()) /60d / recsMinute;
						double minutesToEnd = ((double)(RecordIterator.EUROPEANA_DATASET_SIZE - repository.getCurrentRecordIndex()) / recsMinute) % 60;
//							double minutesToEnd = (double)(RecordIterator.EUROPEANA_DATASET_SIZE - recCnt) / recsMinute;
						System.out.printf("%d recs. (%d ok) - %d recs/min - %d:%d to end\n", repository.getCurrentRecordIndex(), okRecs,
								(int) recsMinute, (int) hoursToEnd, (int) minutesToEnd);
					}
					
					try {
//						edmRdfString=Normalizer.normalize(edmRdfString, Form.NFC);

						if(edmRdfString.contains(searchString)) {
							usesCounter[0]++;
							if(usesCounter[0]<100)
								System.out.println("Found "+usesCounter[0]+" uses");
							else if(usesCounter[0] % 100 == 0)
								System.out.println("Found "+usesCounter[0]+" uses");
						}
						
						okRecs++;
						//DEBUG !!!!!!!!!!!!!!
//							if(okRecs>1000)
//								return false;
						if(MAX_EXPORTED>0 && usesCounter[0]>=MAX_EXPORTED)
							return ProceedOption.STOP;
					} catch (Exception e) {
						System.err.println("Error: " + edmRdfString);
						e.printStackTrace();
					}
					return ProceedOption.CONTINUE;
				}

				@Override
				public ProceedOption handleError(String error, Exception e) {
					System.err.println(error);
					e.printStackTrace();
					return ProceedOption.CONTINUE;
				}
			});
			
		} finally {
			// CLOSE OPERATIONS
			// CLOSE OPERATIONS - END
		}

		System.out.println("Finished!\nFound "+usesCounter[0]+" uses of "+propertyToSearch);
	}
	
	
}
