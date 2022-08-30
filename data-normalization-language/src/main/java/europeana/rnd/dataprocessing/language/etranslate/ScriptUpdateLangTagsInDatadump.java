package europeana.rnd.dataprocessing.language.etranslate;

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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import europeana.rnd.aggregated.RecordIterator;
import europeana.rnd.dataprocessing.ProgressTrackerOnFile;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.util.Handler;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;

public class ScriptUpdateLangTagsInDatadump {

	public static void main(String[] args) throws Exception {
		String outputFolder = null;
		String inputFolder = null;

		if (args != null && args.length >= 2) {
			inputFolder = args[0];
			outputFolder = args[1];
		}else {
			inputFolder = "c://users/nfrei/desktop/data/europeana_dataset";
			outputFolder = "c://users/nfrei/desktop/data/europeana_dataset_etranslate";
		}
		
		File repositoryFolder=new File(inputFolder);
		
		String[] allFiles=repositoryFolder.list();
		Arrays.sort(allFiles);

		for(String filename: allFiles) {
			System.out.println(filename);
			FileInputStream zipFileInputStream = new FileInputStream(new File(repositoryFolder, filename));
			final ZipInputStream zip = new ZipInputStream(zipFileInputStream);
			
			FileOutputStream zipFileOutputStream = new FileOutputStream(new File(outputFolder, filename));
			final ZipOutputStream zipOut = new ZipOutputStream(zipFileOutputStream);
			
			ZipEntry entry = zip.getNextEntry();
			while (entry != null) {
				String edmRdfXmlOrig=IOUtils.toString(zip, StandardCharsets.UTF_8);
				String edmRdfXml=removeSubtags(edmRdfXmlOrig);
				edmRdfXml=NormalisableTags.applyPatterns(edmRdfXml);
				zipOut.putNextEntry(new ZipEntry(entry.getName()));
				IOUtils.write(edmRdfXml, zipOut, StandardCharsets.UTF_8);
				zipOut.closeEntry();
				zip.closeEntry();
				entry = zip.getNextEntry();
			}		
			zip.close();
			zipFileInputStream.close();
			zipOut.close();
			zipFileOutputStream.close();
		}
	}

	private static Pattern subtagPattern=Pattern.compile("( xml:lang\\s*=\\s*\")([^-\"]+)\\-[^\"]+(\")");
	private static String removeSubtags(String edmRdfXml) {
		return subtagPattern.matcher(edmRdfXml).replaceAll("$1$2$3");
	}
	
	
	
	
}
