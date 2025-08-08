package europeana.rnd.dataprocessing.scripts.sgoab;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.RiotException;

import apiclient.google.GoogleApi;
import apiclient.google.sheets.SheetsPrinter;
import apiclient.google.sheets.SheetsReader;
import inescid.http.HttpRequestService;
import inescid.util.AccessException;
import inescid.util.GlobalSingleton;
import inescid.util.HttpRequestServiceSingleton;
import inescid.util.HttpUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.stream.JsonParser;
import javax.management.RuntimeErrorException;
import javax.xml.crypto.dsig.keyinfo.KeyName;

public class ScriptPrepareZooniverseDataset {
	public static void main(String[] args) throws Exception {
		HashMap<Integer, String> userHashes=new HashMap<Integer, String>();
		
		GoogleApi.init("c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json");    
		SheetsPrinter printer=new SheetsPrinter("1D-A6QFh-VFuRUpM_Ui-akgR6fHgR_SdTsjQFKyLFDd8",
				"Cleaned dump");
		SheetsReader reader=new SheetsReader("1D-A6QFh-VFuRUpM_Ui-akgR6fHgR_SdTsjQFKyLFDd8",
				"saint-george-on-a-bike-classifications", "A2:N34873");
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
		Date earliestValidDate=dateFormat.parse("2022-03-01");
		
		printer.printRecord("classification_id","user(anonymized)","created_at","metadata","annotations","subject_data","DEArt_filename", "subject_id");
		for(List<Object> rec: reader) {
			JsonReader annoJson=Json.createReader(new StringReader((String) rec.get(11)));
			JsonValue jsonValue = annoJson.readArray().get(0).asJsonObject().get("value");
			ValueType valueType = jsonValue.getValueType();
			String valStr = jsonValue.toString();
//			if(valueType==ValueType.NULL)
//				continue;
			if(valueType==ValueType.NULL)
				valStr="";
			else
				valStr=valStr.substring(1,valStr.lastIndexOf('"'));
			if(valStr.trim().length()<3 || valStr.trim().toLowerCase().equals("n/a")
					|| valStr.trim().matches("\\d+"))
				continue;
			String created = (String)rec.get(7);
			Date parse = dateFormat.parse(created.substring(0,10));
			if(parse.before(earliestValidDate))
				continue;
			
			
			int userHash=Math.abs(rec.get(1).hashCode());
			if(userHashes.containsKey(userHash) && !userHashes.get(userHash).equals(rec.get(1)))
				throw new RuntimeException("Coliding user hash code");
			userHashes.put(userHash, (String)rec.get(1));
			
			annoJson.close();

			String subjectId = (String) rec.get(13);
			JsonReader subjectJson=Json.createReader(new StringReader((String) rec.get(12)));
			String filename = subjectJson.readObject().getJsonObject(subjectId).getString("Filename");
			subjectJson.close();
			
			if(filename.endsWith("_web.jpg"))
				continue;
			
			printer.print(rec.get(0));			
			printer.print(userHash);			
			printer.print(created);		
			printer.print(rec.get(10));	
			printer.print(rec.get(11));
//			printer.print(valStr);
			printer.print(rec.get(12));
			printer.print(filename);
			printer.print(subjectId);
			printer.println();
			
//			System.out.print(rec.get(0));			
//			printer.print(rec.get(7));			
//			annoJson=Json.createReader(new StringReader((String) rec.get(11)));
//			System.out.print(jsonValue.toString());
//			annoJson.close();
//
//			 subjectJson=Json.createReader(new StringReader((String) rec.get(12)));
//			System.out.print(subjectJson.readObject().getJsonObject(subjectId).getString("Filename"));
//			subjectJson.close();
//			
//			System.out.print(subjectId);
//			System.out.println();
			
		}
		
		printer.close();
		System.out.println("Finished");
	}

}
