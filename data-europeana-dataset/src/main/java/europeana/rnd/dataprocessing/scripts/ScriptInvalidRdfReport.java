package europeana.rnd.dataprocessing.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Rdf;
import inescid.util.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;

public class ScriptInvalidRdfReport {

	public static class RecordIterator {
		File repositoryFolder;
		int startAt=0;
		Lang lang=Lang.RDFXML;
		int recIndex=0;
		
		public RecordIterator(File repositoryFolder) {
			super();
			this.repositoryFolder = repositoryFolder;
		}
		
		public void setStartRecord(int zeroBasedIndex) {
			startAt=zeroBasedIndex;
		}
		
		
		
		public void iterate(Handler<Model, String> handler) throws IOException {
			String[] allFiles=repositoryFolder.list();
			Arrays.sort(allFiles);

			boolean normalize=lang.equals(Lang.RDFXML);
			
			for(String filename: allFiles) {
				System.out.println(filename);
				FileInputStream zipFileInputStream = new FileInputStream(new File(repositoryFolder, filename));
				final ZipInputStream zip = new ZipInputStream(zipFileInputStream);
				ZipEntry entry = zip.getNextEntry();
				while (entry != null) {
					if(recIndex>=startAt) {
						String edmRdfXml="";
						try {
							edmRdfXml=IOUtils.toString(zip, StandardCharsets.UTF_8);
							if(normalize)
								edmRdfXml=Normalizer.normalize(edmRdfXml, Form.NFC);
							
							Model readRdf = readRdf(edmRdfXml, lang);
							if(!handler.handle(readRdf)) {
								zip.closeEntry();
								zip.close();
								zipFileInputStream.close();
								return;
							}
						} catch (Exception e) {
							if(!handler.handleError(filename.substring(0, filename.lastIndexOf('.'))+"/"+entry.getName().substring(entry.getName().lastIndexOf('.')) ,e)) {
								zip.closeEntry();
								zip.close();
								zipFileInputStream.close();
								return;
							}
						} catch (Throwable e) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							throw e;
						}
					}
					zip.closeEntry();
					entry = zip.getNextEntry();
					recIndex++;
				}
				zip.close();
			}
		}
		public int getCurrentRecordIndex() {
			return recIndex;
		}

		public void setLang(Lang lang2) {
			this.lang = lang2;
		}
		
		
	}
	
	
	
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
			outputFolder = "c://users/nfrei/desktop/data/invalid-rdf";
			fileFormat ="XML";
//			europeana.rnd.dataprocessing.dates.DatesJsonWriter.maxFilesPerFolder=10;
//			europeana.rnd.dataprocessing.dates.DatesJsonWriter.maxRecsPerFile=50;
		}

		RecordIterator repository=new RecordIterator(new File(inputFolder));
		
		// INIT OPERATIONS
		File outFile = new File(outputFolder, "invalid-rdf.csv");		
		Lang lang=null;
		if(fileFormat.equals("XML"))
			 lang=Lang.RDFXML;
		else
			lang=Lang.TURTLE;
		repository.setLang(lang);
		// INIT OPERATIONS - END

		
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
							okRecs++;
							//DEBUG !!!!!!!!!!!!!!
//							if(okRecs>1000)
//								return false;
					return true;
				}

				@Override
				public boolean handleError(String error, Exception e) {
					try {
						FileUtils.write(outFile, "\n\""+error+"\",\""+e.getMessage().replace('\"', '-')+"\"", StandardCharsets.UTF_8, true);
						return true;
					} catch (IOException e1) {
						e1.printStackTrace();
						return false;
					}
				}
			});
		} finally {
			// CLOSE OPERATIONS
			// CLOSE OPERATIONS - END
		}
//		csvOut.close();
//		csvBuffer.close();
	}
	
	public static Model readRdf(String contentStr, Lang l) throws RiotException{
		Model model = ModelFactory.createDefaultModel();
		RDFReaderI reader = model.getReader(l.getName());
//		reader.setProperty("allowBadURIs", "true");
		reader.read(model, new StringReader(contentStr), null);
		return model;
	}
	
}
