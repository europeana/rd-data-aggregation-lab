package europeana.rnd.dataprocessing.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
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

public class ScriptExportThumbnailIds {
	
//	static MapOfInts<String> statsPerCho=new MapOfInts<String>();
//	static Map<String, Examples> exampleChosPerType=new HashMap<String, Examples>();
//	static MapOfInts<String> statsPerWebResource=new MapOfInts<String>();
	
	public static void main(String[] args) throws Exception {
//		System.out.println( getMD5("http://collections.rmg.co.uk/mediaLib/308/media-308198/large.jpg"));
//		System.out.println( getMD5_Metis("http://collections.rmg.co.uk/mediaLib/308/media-308198/large.jpg"));
//		System.exit(0);
				
		String inputFolder = null;
		String outputFile = null;
		String fileFormat ="XML";
		int maxExportedParam=0; 
		int maxExportedPerDatasetParam=0; 
		
		if (args != null && args.length >= 3) {
			inputFolder = args[0];
			fileFormat = args[1];
			outputFile = args[2];
			if (args.length >= 4) {
				maxExportedParam = Integer.parseInt(args[3]);
				if (args.length >= 5) 
					maxExportedPerDatasetParam = Integer.parseInt(args[4]);
			}
		}else {
			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset_test2";
//			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset_sample";
			fileFormat ="XML";
			outputFile = "c://users/nfrei/desktop/data/cho-thumbnail-map.csv";
			maxExportedParam=0; 
			maxExportedPerDatasetParam=2; 
		}

		RecordIterator repository=new RecordIterator(new File(inputFolder));
		
		// INIT OPERATIONS
		final int MAX_EXPORTED=maxExportedParam; 
		final int MAX_EXPORTED_PER_DATASET=maxExportedPerDatasetParam; 
		final int[] countExported=new int[] {0};
		final MapOfInts<String> countExportedPerDataset=new MapOfInts<String>();
		
		FileWriterWithEncoding fileWriter=new FileWriterWithEncoding(new File(outputFile), StandardCharsets.UTF_8);
		CSVPrinter csv=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
		csv.printRecord("ProvidedCHO", "Thumbnail URL", "Thumbnail ID");
		
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
					
					
//					System.out.println(choUri);
					try {
						URI previewUrl=null;
						Resource aggEur = EdmRdfUtil.getEuropeanaAggregationResource(edm);
						Statement stPreview=aggEur.getProperty(Edm.preview);
						
						if(stPreview!=null) {
							if(MAX_EXPORTED_PER_DATASET>0) {
								if(countExportedPerDataset.containsKey(dataset) && countExportedPerDataset.get(dataset)>=MAX_EXPORTED_PER_DATASET)
									return ProceedOption.GO_TO_NEXT_DATASET;
							}
							countExportedPerDataset.incrementTo(dataset);
							
							String urlStr = stPreview.getObject().asResource().getURI();
							if(urlStr.startsWith("https://api.europeana.eu/thumbnail/v2/")) {
								System.out.println("TODO: "+urlStr);
//							List<NameValuePair> params = URLEncodedUtils.parse(previewUrl, StandardCharsets.UTF_8);
//							
//							for (NameValuePair param : params) {
//							  System.out.println(param.getName() + " : " + param.getValue());
//							}

							} else if(urlStr.startsWith("https://api.europeana.eu/thumbnail/v3/")) {
								System.out.println("TODO: "+urlStr);								
							}else
								previewUrl=new URI(urlStr);
							
							
//							System.out.println("Preview URL "+previewUrl);
							
							if(previewUrl!=null) {
								csv.printRecord(choUri, previewUrl.toASCIIString(), getMD5(previewUrl.toASCIIString()));
							}
						} else {
							return ProceedOption.CONTINUE;
						}
						
						// CALL OPERATIONS - END
						okRecs++;
						
						//DEBUG !!!!!!!!!!!!!!
//							if(okRecs>1000)
//								return false;
						if(MAX_EXPORTED>0 && countExported[0]>=MAX_EXPORTED)
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
		
		System.out.println("Finished Export of thumb ids");
		
		csv.close();
		fileWriter.close();
			
	}
	
    /**
     * Return MD5 hash of the provided string (usually an image url)
     * @param resourceUrl url for which hash needs to generated
     * @return MD5 hash value
     */
    public static String getMD5(String resourceUrl){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(resourceUrl.getBytes(StandardCharsets.UTF_8));
            final byte[] resultByte = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aResultByte : resultByte) {
                sb.append(Integer.toString((aResultByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
        	e.printStackTrace();
        }
        return "";
    }	
//    /**
//     * Return MD5 hash of the provided string (usually an image url)
//     * @param resourceUrl url for which hash needs to generated
//     * @return MD5 hash value
//     */
//    public static String getMD5_Metis(String stringToMd5){
//        try {
//            byte[] bytes = stringToMd5.getBytes(StandardCharsets.UTF_8.name());
//            byte[] md5bytes = MessageDigest.getInstance("MD5").digest(bytes);
//            return String.format("%032x", new BigInteger(1, md5bytes));
//          } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
//            throw new RuntimeException("Could not compute md5 hash", e);
//          }
//    }
	
	
}
