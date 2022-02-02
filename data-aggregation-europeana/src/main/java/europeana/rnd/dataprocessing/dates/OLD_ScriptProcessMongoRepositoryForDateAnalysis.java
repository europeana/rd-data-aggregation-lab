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
import org.apache.jena.riot.RiotException;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.tiers.model.MetadataTier;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import europeana.rnd.dataprocessing.LangTagsResult.IN;
import europeana.rnd.dataprocessing.LangTagsResult.SOURCE;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.dataset.profile.tiers.TiersCalculation;
import inescid.europeanarepository.EdmMongoServer;
import inescid.europeanarepository.EdmMongoServer.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;

public class OLD_ScriptProcessMongoRepositoryForDateAnalysis {

	public static void main(String[] args) throws Exception {
		String outputFolder = null;

		if (args != null && args.length >= 1) {
				outputFolder = args[0];
		}else {
			outputFolder = "c://users/nfrei/desktop/data/dates";
			europeana.rnd.dataprocessing.dates.DatesJsonWriter.maxFilesPerFolder=10;
			europeana.rnd.dataprocessing.dates.DatesJsonWriter.maxRecsPerFile=20;
		}

//		Global.init_componentDataRepository(repoFolder);
//		Global.init_enableComponentHttpRequestCache();
//		Repository repository = Global.getDataRepository();
		EdmMongoServer edmMongo = new EdmMongoServer("mongodb://rnd-2.eanadev.org:27027/admin",
				"metis-preview-production-2");
		
		// INIT OPERATIONS
		DatesHandler datesHandler=new DatesHandler(outputFolder);
		
		// INIT OPERATIONS - END

		final ProgressTrackerOnFile tracker=new ProgressTrackerOnFile(new File(outputFolder, OLD_ScriptProcessMongoRepositoryForDateAnalysis.class.getSimpleName()+"_progress.txt"));
		final int offset=tracker.getTokenAsInt();
		System.out.println("Starting at mongo offset "+offset);

		try {
			edmMongo.forEach(FullBeanImpl.class, new Handler<FullBeanImpl>() {
				Date start = new Date();
				int recCnt = offset;
				int okRecs = 0;
				
				public boolean handle(FullBeanImpl fb) {
//					System.out.println(".");
					try {
						String recId = fb.getAbout().substring(1);
						recCnt++;
						if (recCnt % 10000 == 0 || recCnt == 10) {
	//						csvOut.flush();
	//						csvBuffer.flush();
							Date now = new Date();
							long elapsedPerRecord = (now.getTime() - start.getTime()) / recCnt;
							double recsMinute = 60000 / (elapsedPerRecord==0 ? 1 : elapsedPerRecord);						
							double hoursToEnd = (double)(58000000 - recCnt) /60d / recsMinute;
							double minutesToEnd = ((double)(58000000 - recCnt) / recsMinute) % 60;
//							double minutesToEnd = (double)(58000000 - recCnt) / recsMinute;
							System.out.printf("%d recs. (%d ok) - %d recs/min - %d:%d to end\n", recCnt, okRecs,
									(int) recsMinute, (int) hoursToEnd, (int) minutesToEnd);
						}
						
						// CHECK PROCESSED ALREADY

						// CHECK PROCESSED ALREADY - END
						
						try {
							String edmRdfXml = EdmUtils.toEDM(fb);

							edmRdfXml=Normalizer.normalize(edmRdfXml, Form.NFC);
							
							// CALL OPERATIONS
							Model edm = RdfUtil.readRdf(edmRdfXml, org.apache.jena.riot.Lang.RDFXML);
							
							datesHandler.handle(recId, edm, recCnt);
							// CALL OPERATIONS - END
							okRecs++;
							
							//DEBUG !!!!!!!!!!!!!!
//							if(okRecs>1000)
//								return false;
							
						} catch (RiotException e) {
							System.err.println("Error reading RDF: " + recId);
							System.err.println(e.getMessage());
						} catch (Exception e) {
							System.err.println("Error: " + recId);
							e.printStackTrace();
						}
					}finally {
						try {
							tracker.track(recCnt);
						} catch (IOException e) {
							e.printStackTrace();
							throw new RuntimeException(e.getMessage(), e);
						}
					}
					return true;
				}
			}, offset);
		} finally {
			// CLOSE OPERATIONS
			datesHandler.finalize();
			// CLOSE OPERATIONS - END
		}
//		csvOut.close();
//		csvBuffer.close();
	}
}
