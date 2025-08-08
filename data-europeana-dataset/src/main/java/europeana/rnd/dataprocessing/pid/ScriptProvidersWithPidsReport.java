package europeana.rnd.dataprocessing.pid;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import apiclient.google.sheets.SheetsReader;
import europeana.rnd.api.entity.OrganizationApi;
import europeana.rnd.dataprocessing.pid.IdsInRecord.DetectedPid;
import inescid.dataaggregation.data.model.Edm;
import inescid.http.HttpRequest;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;

public class ScriptProvidersWithPidsReport {

	String spreadsheetId;
	
	public ScriptProvidersWithPidsReport(String spreadsheetId) {
		super();
		this.spreadsheetId = spreadsheetId;
	}

	public void process() throws IOException, AccessException, InterruptedException {
		SheetsReader inSheet=new SheetsReader(spreadsheetId, "Stats by provider", "A3:L3741");
		SheetsPrinter outSheet=new SheetsPrinter(spreadsheetId, "Providers with PIDs");
		outSheet.printRecord("Data provider URI", "Data provider name", "Nr- of records without a PID", "Nr. of records with a PID", "With ARK", "With HANDLE", "With DOI", "With URN", "With Purl", "Providers");
		for(List<Object> line: inSheet) {
			String dataProvider=(String)line.get(0);
			int withPid=Integer.parseInt(line.get(2).toString());
			
			if(withPid>0) {
				String dataProviderUri="";
				if(dataProvider.startsWith("http://")) {
					dataProviderUri=dataProvider;
					dataProvider= OrganizationApi.getOrganizationName(dataProviderUri, true);
				}
				outSheet.print(dataProviderUri, dataProvider);
//				outSheet.print(dataProviderUri, dataProvider, withPid, line.get(1));

				for(int i=1; i<=7; i++)
					outSheet.print(line.get(i).toString());
				for(int i=8; i<line.size(); i++) {
					if(line.get(i)!=null ) {
						String providerUri=line.get(i).toString();
						if(providerUri.startsWith("http://")) {
							outSheet.print(OrganizationApi.getOrganizationName(providerUri, true));							
						} else
							outSheet.print(providerUri);							
					}
				}
				outSheet.println();
			}
		}
		outSheet.close();
	}



	public static void main(String[] args) throws Exception {
		if(new File("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json").exists())
			GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
		else
			GoogleApi.init("/home/nfreire/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    		
		
		String spreadsheetId = "1UIycwKV5Jrhm_tPPhN1cgfi6lZuOEnUg2sWbPsphiGY";

		if (args != null) {
			if (args.length >= 1) {
				spreadsheetId = args[0];
			}
		}
		
		ScriptProvidersWithPidsReport processor=new ScriptProvidersWithPidsReport(spreadsheetId);
		processor.process();
	}
	
	

	
	
}
