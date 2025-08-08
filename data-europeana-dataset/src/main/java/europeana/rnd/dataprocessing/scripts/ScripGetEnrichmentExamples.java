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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.aggregated.RecordIterator.Handler;
import europeana.rnd.aggregated.RecordIterator.Handler.ProceedOption;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import europeana.rnd.dataprocessing.scripts.Examples.Example;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Ebucore;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;
import inescid.util.europeana.EdmRdfUtil;

public class ScripGetEnrichmentExamples {
	
//	static MapOfInts<String> statsPerCho=new MapOfInts<String>();
//	static Map<String, Examples> exampleChosPerType=new HashMap<String, Examples>();
//	static MapOfInts<String> statsPerWebResource=new MapOfInts<String>();
	
	public static void main(String[] args) throws Exception {
		String inputFolder = null;
		String outputFile = null;
		String fileFormat ="XML";
		
		if (args != null && args.length >= 2) {
			inputFolder = args[0];
			fileFormat = args[1];
			outputFile = args[2];
		}else {
			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset_test_epf";
			fileFormat ="XML";
			outputFile = "c://users/nfrei/desktop/data/enrichment-sample.csv";
		}

		RecordIterator repository=new RecordIterator(new File(inputFolder));
		
		// INIT OPERATIONS
		int MAX_EXPORTED=10000; 
		int MAX_EXPORTED_PER_DATASET=50; 
		boolean checkOnline=false;
		final int[] countExported=new int[] {0};
		final MapOfInts<String> countExportedPerDataset=new MapOfInts<String>();
		
		FileWriterWithEncoding fileWriter=new FileWriterWithEncoding(new File(outputFile), StandardCharsets.UTF_8);
		CSVPrinter csv=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
		csv.printRecord("ProvidedCHO", "Property", "Entity");
		
		// INIT OPERATIONS - END

		if(fileFormat.equals("XML"))
			repository.setLang(Lang.RDFXML);
		else
			repository.setLang(Lang.TURTLE);
		
		try {
			repository.iterate(new Handler<Model, String>() {
				Date start = new Date();
				int okRecs = 0;
				Model onlineVersion=null;

				public ProceedOption handle(Model edm, String dataset, String recId) throws Exception {
					onlineVersion=null;
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
					
					if(countExportedPerDataset.get(dataset)>=MAX_EXPORTED_PER_DATASET)
						return ProceedOption.CONTINUE;
					try {
						Resource proxyEur = EdmRdfUtil.getProxyOfEuropeana(edm);
						Resource proxyProvider = null;
						for(Statement st: proxyEur.listProperties().toList() ){
							if(st.getObject().isResource()
								&& (st.getObject().asResource().getURI().startsWith("http://data.europeana.eu/agent")
								    || st.getObject().asResource().getURI().startsWith("http://data.europeana.eu/concept")
								    || st.getObject().asResource().getURI().startsWith("http://data.europeana.eu/place")
//								    || st.getObject().asResource().getURI().startsWith("http://data.europeana.eu/timespan")
									)) {
								boolean isEnrichment=false;
								Resource rdfType = st.getObject().asResource().getProperty(Rdf.type).getObject().asResource();
								Property property= st.getPredicate();
								if(rdfType.equals(Edm.Agent)) {
									if(property.equals(Dc.creator) || property.equals(Dc.creator)) 
										isEnrichment=true;
								} else if(rdfType.equals(Skos.Concept)) {
									if(property.equals(Dc.subject) || property.equals(Dc.type) || property.equals(Dc.format) || property.equals(DcTerms.medium)) {
										isEnrichment=true;
									}
								} else if(rdfType.equals(Edm.Place)) {
									if(property.equals(Dc.coverage) || property.equals(DcTerms.spatial)) 
										isEnrichment=true;
								}

								if(isEnrichment && proxyProvider==null)
									proxyProvider = EdmRdfUtil.getProxyOfDataProvider(edm);
								if(isEnrichment && !proxyContainsResource(proxyProvider, property)) {
									if(!checkOnline || existsOnline(choUri, st)) {
										csv.printRecord(choUri, st.getPredicate().getURI(), st.getObject().asResource().getURI());
										countExported[0]++;
										countExportedPerDataset.incrementTo(dataset);
									}
								}
							}
							
//								webResources.add(st.getObject().asResource());
						}
						
						// CALL OPERATIONS - END
						okRecs++;
						
						//DEBUG !!!!!!!!!!!!!!
//							if(okRecs>1000)
//								return false;
						if(countExported[0]>=MAX_EXPORTED)
							return ProceedOption.STOP;
					} catch (RiotException e) {
						System.err.println("Error reading RDF: " + choUri);
						System.err.println(e.getMessage());
					} catch (Exception e) {
						System.err.println("Error: " + choUri);
						e.printStackTrace();
					}
					return ProceedOption.CONTINUE;
				}

				private boolean existsOnline(String choUri, Statement st) {
					try {
						if (onlineVersion==null)
							onlineVersion = RdfUtil.readRdfFromUri(choUri);
						StmtIterator stms = onlineVersion.listStatements(st.getSubject(), st.getPredicate(), st.getObject());
						return stms.hasNext();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}

				private boolean proxyContainsResource(Resource proxyProvider, Property property) {
					for(Statement st: proxyProvider.listProperties(property).toList()) {
						if(st.getObject().isResource()) {
//							System.out.println(st);
							return true;
						}
					}
					return false;
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
		csv.close();
		fileWriter.close();
			
	}
	
	
	
	
}
