package europeana.rnd.dataprocessing.scripts.sgoab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JsonAnnotatedCho {
	public String choUri;
	public ArrayList<Annotation> annotations=new ArrayList<Annotation>();
	public JsonAnnotatedCho(String choUri, JsonObject rootJson) {
		this.choUri = choUri;
		JsonObject annotationObj = rootJson.getJsonObject("output").getJsonObject("object_detection");

		
//		JsonArray classesJson = annotationObj.getJsonArray("classes");
		JsonArray classesJson = annotationObj.getJsonArray("classes-labels");
		for(JsonValue classJson: classesJson) {
			annotations.add(new Annotation(((JsonString)classJson).getString()));
		}
		int idx=0;
		JsonArray scoresJson = annotationObj.getJsonArray("scores");
		for(JsonValue scoreJson: scoresJson) {
//			annotations.get(idx).confidence=((JsonString)scoreJson).getString().trim();
			annotations.get(idx).confidence=String.valueOf((int)(((JsonNumber)scoreJson).doubleValue()*100))+"%";
			idx++;
		}
		idx=0;
//		JsonArray bbxsJson = annotationObj.getJsonArray("bbxs");
		JsonArray bbxsJson = annotationObj.getJsonArray("bbx");
		for(JsonValue bbxJson: bbxsJson) {
			annotations.get(idx).x=(int)(((JsonNumber)bbxJson.asJsonArray().get(0)).doubleValue()*100);
			annotations.get(idx).y=(int)(((JsonNumber)bbxJson.asJsonArray().get(1)).doubleValue()*100);
			annotations.get(idx).w=(int)(((JsonNumber)bbxJson.asJsonArray().get(2)).doubleValue()*100);
			annotations.get(idx).h=(int)(((JsonNumber)bbxJson.asJsonArray().get(3)).doubleValue()*100);
//			annotations.get(idx).x=((JsonNumber)bbxJson.asJsonArray().get(0)).intValue();
//			annotations.get(idx).y=((JsonNumber)bbxJson.asJsonArray().get(1)).intValue();
//			annotations.get(idx).w=((JsonNumber)bbxJson.asJsonArray().get(2)).intValue();
//			annotations.get(idx).h=((JsonNumber)bbxJson.asJsonArray().get(3)).intValue();
			idx++;
		}
	}
	
	@Override
	public String toString() {
		String ret= choUri +"[";
		for(Annotation anno: annotations) 
			ret+=anno+" ";
		ret+="]";
		return ret;
	}

	public void removeAnnotationNotIn(Set<String> tags) {
		Iterator<Annotation> it=annotations.iterator();
		while(it.hasNext()) {
			Annotation anno=it.next();
			if (!tags.contains(anno.label)) {
				it.remove();
			}
		}
		
		
		
	}
}