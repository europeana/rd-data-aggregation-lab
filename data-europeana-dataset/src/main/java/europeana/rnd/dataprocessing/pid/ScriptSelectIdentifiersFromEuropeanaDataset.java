package europeana.rnd.dataprocessing.pid;

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

public class ScriptSelectIdentifiersFromEuropeanaDataset {

	public static void main(String[] args) throws Exception {
		String outputFolder = null;
		String inputFolder = null;
		String fileFormat ="XML";

		if (args != null && args.length >= 3) {
			inputFolder = args[0];
			outputFolder = args[1];
			fileFormat = args[2];
		}else {
			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset";
			outputFolder = "c://users/nfrei/desktop/data/pid";
			fileFormat ="XML";
//			europeana.rnd.dataprocessing.dates.DatesJsonWriter.maxFilesPerFolder=10;
//			europeana.rnd.dataprocessing.dates.DatesJsonWriter.maxRecsPerFile=50;
		}

		RecordIterator repository=new RecordIterator(new File(inputFolder));
		
		// INIT OPERATIONS
		IdsHandler handler=new IdsHandler(outputFolder);
		
		// INIT OPERATIONS - END

		if(fileFormat.equals("XML"))
			repository.setLang(Lang.RDFXML);
		else
			repository.setLang(Lang.TURTLE);
		
		try {
			repository.iterate(new Handler<Model, String>() {
				Date start = new Date();
				int okRecs = 0;
				
				public boolean handle(Model edm) {
//					System.out.println(".");
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
						handler.handle(edm, repository.getCurrentRecordIndex());
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
			handler.finalize();
			// CLOSE OPERATIONS - END
		}
//		csvOut.close();
//		csvBuffer.close();
	}
}
