package europeana.rnd.dataprocessing.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;
import inescid.util.europeana.EdmRdfUtil;

public class ScriptGetWithWikidataEntityDatadump {

	public static void main(String[] args) throws Exception {
		String outputFolderStr = null;
		String inputFolder = null;

		if (args != null && args.length >= 2) {
			inputFolder = args[0];
			outputFolderStr = args[1];
		}else {
			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset_test_epf";
			outputFolderStr = "c://users/nfrei/desktop/data/europeana_dataset_with_wikidata";
		}
		
		File outputFolder = new File(outputFolderStr);
		if(!outputFolder.exists())
			outputFolder.mkdirs();
		
		File repositoryFolder=new File(inputFolder);
		File urisExportFile=new File(outputFolder, "cho_uris.txt");
		
		String[] allFiles=repositoryFolder.list();
		Arrays.sort(allFiles);

		for(String filename: allFiles) {
			System.out.println(filename);
			FileInputStream zipFileInputStream = new FileInputStream(new File(repositoryFolder, filename));
			final ZipInputStream zip = new ZipInputStream(zipFileInputStream);
			
			File zipOutFile = new File(outputFolder, filename);
			FileOutputStream zipFileOutputStream = new FileOutputStream(zipOutFile);
			final ZipOutputStream zipOut = new ZipOutputStream(zipFileOutputStream);
			boolean zipHasData=false;
			ZipEntry entry = zip.getNextEntry();
			while (entry != null) {
				String edmRdfXml=IOUtils.toString(zip, StandardCharsets.UTF_8);
				edmRdfXml=Normalizer.normalize(edmRdfXml, Form.NFC);
				Model model = RdfUtil.readRdf(edmRdfXml, Lang.RDFXML);
				
//				RdfUtil.printOutRdf(model);
				
				boolean export=false;
				RES: for(Resource ag: RdfUtil.findResourcesOfType(model, Edm.Agent)) {
					if(ag.getURI().startsWith("http://www.wikidata.org/")) {
						export=true;
						break;
					}
					for(Statement st: ag.listProperties(Owl.sameAs).toList()) {
						String uri = st.getObject().asResource().getURI();
						if(uri.startsWith("http://www.wikidata.org/")) {
							export=true;
							break RES;
						}
					}
//					if(st.getObject().isResource() && st.getObject().isURIResource()) {
//						String uri = st.getObject().asResource().getURI();
//						if(uri.startsWith("http://www.wikidata.org/")) {
//							Resource type=st.getSubject().getPropertyResourceValue(Rdf.type); 
//							if(type.equals(Edm.Agent)) {
////								if(type.equals(Edm.Place) || type.equals(Edm.Agent) || type.equals(Skos.Concept)) {
//								export=true;
//								break;
//							}
//						}
//					}
				}
				
				if(export) {
					String name = entry.getName();
					name=name.substring(0, name.length()-4)+".ttl";
					zipOut.putNextEntry(new ZipEntry(name));
					
					String edmTtlString=RdfUtil.writeRdfToString(model, Lang.TTL);
					
					IOUtils.write(edmTtlString, zipOut, StandardCharsets.UTF_8);
					zipOut.closeEntry();	
					zipHasData=true;
					FileUtils.write(urisExportFile, EdmRdfUtil.getProvidedChoResource(model).getURI()+"\n", StandardCharsets.UTF_8, true);
				}
				zip.closeEntry();
				entry = zip.getNextEntry();
			}		
			zip.close();
			zipFileInputStream.close();
			zipOut.close();
			zipFileOutputStream.close();
			if(!zipHasData)
				zipOutFile.delete();
		}
	}

	
	
	
	
}
