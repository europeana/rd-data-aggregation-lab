package europeana.rnd.aggregated;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotException;

import inescid.util.Handler;
import inescid.util.RdfUtil;

public class RecordIterator {
	File repositoryFolder;
	int startAt=0;
	Lang lang=Lang.TURTLE;
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
						
						Model readRdf = RdfUtil.readRdf(edmRdfXml, lang);
						if(!handler.handle(readRdf)) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							return;
						}
					} catch (Throwable e) {
						if(!handler.handleError(entry.getName() ,(Exception)e)) {
							zip.closeEntry();
							zip.close();
							zipFileInputStream.close();
							return;
						}
					}
				}
				zip.closeEntry();
				entry = zip.getNextEntry();
				recIndex++;
			}
			zip.close();
		}
				
//				RDFParserBuilder builder = RDFParser.create().checking(false).forceLang(Lang.TURTLE);
//				//the zip input stream is wrappped to prevent the parser from closing it.
//				try {
//					builder.source(new InputStream() { 
//						@Override
//						public int read() throws IOException {
//							return zip.read();
//						}
//						@Override
//						public int read(byte[] b) throws IOException {
//							return zip.read(b);
//						}
//						@Override
//						public int read(byte[] b, int off, int len) throws IOException {
//							return zip.read(b, off, len);
//						}
//						@Override
//						public int available() throws IOException {
//							return zip.available();
//						}
//						@Override
//						public void close() throws IOException {
////						super.close(); 
//						}
//					}).parse(handler);
//				} catch (RiotException e) {
//					//Invalid RDF
//					//ignore error and proceed to next file
//				}
				
	}

	public void setLang(Lang lang) {
		this.lang = lang;
	}

	public int getCurrentRecordIndex() {
		return recIndex;
	}
	
	
}
