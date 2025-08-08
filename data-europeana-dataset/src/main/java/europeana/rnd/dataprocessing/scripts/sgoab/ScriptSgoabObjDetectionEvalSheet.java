package europeana.rnd.dataprocessing.scripts.sgoab;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.RiotException;

import inescid.http.HttpRequestService;
import inescid.util.AccessException;
import inescid.util.GlobalSingleton;
import inescid.util.HttpRequestServiceSingleton;
import inescid.util.HttpUtil;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParser;
import javax.xml.crypto.dsig.keyinfo.KeyName;

public class ScriptSgoabObjDetectionEvalSheet {

	public static void main(String[] args) throws Exception {
//		int maxSample = 5;
		int maxSample = Integer.MAX_VALUE;
		int sampleCnt = 0;

		File sourceFolder = new File("C:\\Users\\nfrei\\Desktop\\data\\sgoab\\enrichments-object-detection-eval2");

		SheetsFormWriter sheetsWriter = new SheetsFormWriter("1xnNh14tESiIAHEbelIKe_w0wH5AXy5Z9qDY3GN-cdQw",
				"Evaluation4");
		sheetsWriter.init(null);

		File annoFile = new File(sourceFolder, "object-detection-eval2.csv");
		CSVParser csvParser = new CSVParser(new FileReader(annoFile), CSVFormat.DEFAULT);
		for (CSVRecord rec : csvParser) {
			if (!rec.get(0).equals("success"))
				continue;
//				String choUri=rec(1);
			JsonParser parser = Json.createParser(new StringReader(rec.get(2)));
			parser.next();
			JsonObject rootJson = parser.getObject();
			String choUri = rootJson.getString("europeana_id");
			try {
				JsonAnnotatedCho cho = new JsonAnnotatedCho(choUri, rootJson);
				if (!cho.annotations.isEmpty()) {
					EdmMetadata edm = new EdmMetadata(cho.choUri);
					sheetsWriter.writeToEvaluationSheet(cho, edm);
					sampleCnt++;
					if(sampleCnt>=maxSample)
						break;
				} else {
					System.out.println("No enrichments for " + choUri);
				}
			} catch (AccessException | InterruptedException | IOException e) {
				System.err.println("Error reading " + choUri + ":\n" + e.getMessage());
				e.printStackTrace();
			}
		}
		sheetsWriter.close();
		System.out.println("Finished");
	}

}
