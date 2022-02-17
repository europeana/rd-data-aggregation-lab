package europeana.rnd.dataprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.util.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;

public class EuropeanaDatasetProcessor {
	
	public static void run(String[] args, EuropeanaDatasetProcessorHandler handler) throws Exception {
		handler.initParameters(args);
		File inputFolder=handler.getEuropeanaDatasetFolder();
		

		System.out.println("Processing "+inputFolder.getAbsolutePath());
		
		RecordIterator repository=new RecordIterator(inputFolder);
		
		handler.initProcessing();

		File workingFolder=handler.getWorkingFolder();

		final ProgressTrackerOnFile tracker=workingFolder==null ? null : new ProgressTrackerOnFile(new File(workingFolder, EuropeanaDatasetProcessor.class.getSimpleName()+"_progress.txt"));
		if(tracker!=null) {
			int offset=tracker.getTokenAsInt();
			System.out.println("Starting at offset "+offset);
			repository.setStartRecord(offset);
		}
		repository.setLang(Lang.RDFXML);
//		repository.setLang(Lang.TURTLE);
		
		try {
			repository.iterate(new Handler<Model, String>() {
				Date start = new Date();
				int okRecs = 0;
				
				public boolean handle(Model edm) {
//					System.out.println(".");
					try {
//						String recId = fb.getAbout().substring(1);
						if (repository.getCurrentRecordIndex()!=0 && (repository.getCurrentRecordIndex() % 10000 == 0 || repository.getCurrentRecordIndex() == 10)) {
	//						csvOut.flush();
	//						csvBuffer.flush();
							Date now = new Date();
							long elapsedPerRecord = (now.getTime() - start.getTime()) / repository.getCurrentRecordIndex();
							double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);						
							double hoursToEnd = (double)(58000000 - repository.getCurrentRecordIndex()) /60d / recsMinute;
							double minutesToEnd = ((double)(58000000 - repository.getCurrentRecordIndex()) / recsMinute) % 60;
//							double minutesToEnd = (double)(58000000 - recCnt) / recsMinute;
							System.out.printf("%d recs. (%d ok) - %d recs/min - %d:%d to end\n", repository.getCurrentRecordIndex(), okRecs,
									(int) recsMinute, (int) hoursToEnd, (int) minutesToEnd);
						}
						
						Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
						String choUri = choRes.getURI();
						try {
							if(!handler.handleRecord(edm, repository.getCurrentRecordIndex()))
								return false;
							okRecs++;
							
							//DEBUG !!!!!!!!!!!!!!
//							if(okRecs>1000)
//								return false;
						} catch (RiotException e) {
							System.err.println("Error reading RDF: " + choUri);
							System.err.println(e.getMessage());
						} catch (Exception e) {
							System.err.println("Error: " + choUri);
							e.printStackTrace();
						}
					}finally {
						try {
							if(tracker!=null)
								tracker.track(repository.getCurrentRecordIndex());
						} catch (IOException e) {
							e.printStackTrace();
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					return true;
				}

				@Override
				public boolean handleError(String error, Exception e) {
					System.err.println(error);
					e.printStackTrace();
					return true;
				}
			});
		} finally {
			handler.closeProcessing();
		}
	}
}
