package europeana.rnd.dataprocessing.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.aggregated.RecordIterator.Handler;
import europeana.rnd.aggregated.RecordIterator.Handler.ProceedOption;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import europeana.rnd.dataprocessing.scripts.Examples.Example;
import inescid.dataaggregation.data.model.Ebucore;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.datastruct.MapOfInts.Sort;

public class ScripGetMediaTypeStatsFromEuropeanaDataset {
	static MapOfInts<String> statsPerCho=new MapOfInts<String>();
	static Map<String, Examples> exampleChosPerType=new HashMap<String, Examples>();
	static MapOfInts<String> statsPerWebResource=new MapOfInts<String>();
	
	public static void main(String[] args) throws Exception {
		String inputFolder = null;
		String fileFormat ="XML";

		if (args != null && args.length >= 2) {
			inputFolder = args[0];
			fileFormat = args[1];
		}else {
			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset_test";
			fileFormat ="XML";
		}

		RecordIterator repository=new RecordIterator(new File(inputFolder));
		
		// INIT OPERATIONS
		
		// INIT OPERATIONS - END

		if(fileFormat.equals("XML"))
			repository.setLang(Lang.RDFXML);
		else
			repository.setLang(Lang.TURTLE);
		
		try {
			repository.iterate(new Handler<Model, String>() {
				Date start = new Date();
				int okRecs = 0;

				public ProceedOption handle(Model edm, String dataset, String recId) throws Exception {
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
						Set<String> typesFound=new HashSet<String>();
//						Set<Resource> webResources = RdfUtil.findResourcesOfType(edm, Edm.WebResource);
						Set<Resource> webResources = new HashSet<Resource>();
						for(Statement st: edm.listStatements((Resource)null, Edm.isShownBy, (RDFNode) null).toList()){
							if(st.getObject().isResource())
								webResources.add(st.getObject().asResource());
						}
						for(Statement st: edm.listStatements((Resource)null, Edm.hasView, (RDFNode) null).toList()){
							if(st.getObject().isResource())
								webResources.add(st.getObject().asResource());
						}
						
						for(Resource webRes: webResources) {
							Statement mimeSt = webRes.getProperty(Ebucore.hasMimeType);
							String mime="unknown";
							if(mimeSt!=null)
								mime=mimeSt.getObject().asLiteral().getString();
							statsPerWebResource.incrementTo(mime);
							typesFound.add(mime);
						}
						for(String mime: typesFound) {
							Examples examples = exampleChosPerType.get(mime);
							if(examples==null) {
								examples=new Examples();
								exampleChosPerType.put(mime, examples);
							}
							examples.add(choUri, choUri);
							statsPerCho.incrementTo(mime);
						}
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
		
		FileWriterWithEncoding fwriter=new FileWriterWithEncoding("mime-type-stats-cho.csv", StandardCharsets.UTF_8);
		statsPerCho.writeCsv(fwriter, Sort.BY_KEY_ASCENDING);
		fwriter.close();

		fwriter=new FileWriterWithEncoding("mime-type-example.csv", StandardCharsets.UTF_8);
		CSVPrinter printer=new CSVPrinter(fwriter, CSVFormat.DEFAULT);
		for(Entry<String, Examples>  r: exampleChosPerType.entrySet()) {
			printer.print(r.getKey().toString());
			for(Example ex: r.getValue().getSample())
				printer.print(ex.uri);
			printer.println();
		}
		printer.close();
		
		fwriter=new FileWriterWithEncoding("mime-type-stats-web-resource.csv", StandardCharsets.UTF_8);
		statsPerWebResource.writeCsv(fwriter, Sort.BY_KEY_ASCENDING);
		fwriter.close();
		
	}
}
