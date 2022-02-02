package europeana.rnd.dataprocessing.dates;

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
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.tiers.model.MetadataTier;
import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import europeana.rnd.dataprocessing.LangTagsResult.IN;
import europeana.rnd.dataprocessing.LangTagsResult.SOURCE;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.dataset.profile.tiers.TiersCalculation;
import inescid.util.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;

public class ScriptProcessEuropeanaDatasetForDateAnalysis {

	public static void main(String[] args) throws Exception {
		String outputFolder = null;
		String inputFolder = null;

		if (args != null && args.length >= 2) {
				inputFolder = args[0];
				outputFolder = args[1];
		}else {
			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset";
			outputFolder = "c://users/nfrei/desktop/data/dates";
			europeana.rnd.dataprocessing.dates.DatesJsonWriter.maxFilesPerFolder=10;
			europeana.rnd.dataprocessing.dates.DatesJsonWriter.maxRecsPerFile=20;
		}

		RecordIterator repository=new RecordIterator(new File(inputFolder));
		
		// INIT OPERATIONS
		DatesHandler datesHandler=new DatesHandler(outputFolder);
		
		// INIT OPERATIONS - END

		final ProgressTrackerOnFile tracker=new ProgressTrackerOnFile(new File(outputFolder, ScriptProcessEuropeanaDatasetForDateAnalysis.class.getSimpleName()+"_progress.txt"));
		final int offset=tracker.getTokenAsInt();
		System.out.println("Starting at offset "+offset);
		repository.setStartRecord(offset);
//		repository.setLang(Lang.RDFXML);
		repository.setLang(Lang.TURTLE);
		
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
						
						// CHECK PROCESSED ALREADY

						// CHECK PROCESSED ALREADY - END
						Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
						String choUri = choRes.getURI();
						try {
							datesHandler.handle(choUri, edm, repository.getCurrentRecordIndex());
							// CALL OPERATIONS - END
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
			// CLOSE OPERATIONS
			datesHandler.finalize();
			// CLOSE OPERATIONS - END
		}
//		csvOut.close();
//		csvBuffer.close();
	}
}
