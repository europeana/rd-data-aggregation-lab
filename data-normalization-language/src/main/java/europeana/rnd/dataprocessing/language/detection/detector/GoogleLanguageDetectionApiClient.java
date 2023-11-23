package europeana.rnd.dataprocessing.language.detection.detector;

import com.google.cloud.translate.v3.DetectLanguageRequest;
import com.google.cloud.translate.v3.DetectLanguageResponse;
import com.google.cloud.translate.v3.DetectedLanguage;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslationServiceClient;

import europeana.rnd.dataprocessing.language.detection.Result;
import inescid.util.AccessException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GoogleLanguageDetectionApiClient extends Detector {
	String projectId = "rd-team-experiments";

	public GoogleLanguageDetectionApiClient() {
		super("Google");
	}

	public static void main(String[] args) throws Exception {
		GoogleLanguageDetectionApiClient detector = new GoogleLanguageDetectionApiClient();
		Result result = detector.detectLanguage("Um pouco de texto em Português.");
		System.out.println(result);
	}
//
//		System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
//		// GOOGLE_APPLICATION_CREDENTIALS=C:/users/nfrei/.credentials/rd-team-experiments-48acf9dd8f83.json
//
//		detectLanguage(projectId, text);
//	}

	@Override
	public Result detectLanguage(String text) throws AccessException {

		// Initialize client that will be used to send requests. This client only needs
		// to be created
		// once, and can be reused for multiple requests. After completing all of your
		// requests, call
		// the "close" method on the client to safely clean up any remaining background
		// resources.
		try (TranslationServiceClient client = TranslationServiceClient.create()) {
			// Supported Locations: `global`, [glossary location], or [model location]
			// Glossaries must be hosted in `us-central1`
			// Custom Models must use the same location as your model. (us-central1)
			LocationName parent = LocationName.of(projectId, "global");

			// Supported Mime Types:
			// https://cloud.google.com/translate/docs/supported-formats
			DetectLanguageRequest request = DetectLanguageRequest.newBuilder().setParent(parent.toString())
					.setMimeType("text/plain").setContent(text).build();

			DetectLanguageResponse response = client.detectLanguage(request);

			Result res=new Result();
			for (DetectedLanguage language : response.getLanguagesList()) {			
				res.add(language.getLanguageCode(), language.getConfidence());
			}
			return res;
		} catch (IOException e) {
			throw new AccessException(e.getMessage(), e);
		}
	}

}