package europeana.rnd.dataprocessing.language.iana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import europeana.rnd.dataprocessing.language.iana.Subtag.SubtagType;

public class Registry {
	
	HashMap<SubtagType, HashMap<String, Subtag>> subtags=new HashMap<Subtag.SubtagType, HashMap<String,Subtag>>();	
	
	public Registry() {
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("language-subtag-registry.txt");
			readRegistry(is);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	protected void readRegistry(InputStream is) throws IOException {
		BufferedReader reader=new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		Subtag currentTag=null;
		for(String line = reader.readLine() ; line!=null ; line=reader.readLine() ) {
			if(line.startsWith("%%")) {
				if (currentTag!=null && currentTag.getText()!=null){
					HashMap<String, Subtag> subtagsOfType = subtags.get(currentTag.getType());
					if(subtagsOfType == null) {
						subtagsOfType=new HashMap<String, Subtag>();
						subtags.put(currentTag.getType(), subtagsOfType);
					}
					subtagsOfType.put(currentTag.getText().toLowerCase(), currentTag);
				}
				currentTag=new Subtag();
			} else if (line.startsWith("Type: ")) {
				currentTag.setType(SubtagType.valueOf(line.substring("Type: ".length()).toUpperCase()));
			} else if (line.startsWith("Subtag: ")) {
				currentTag.setText(line.substring("Subtag: ".length()));					
			} else if (line.startsWith("Description: ")) {
				currentTag.addDescription((line.substring("Description: ".length())));	
			} else if (line.startsWith("Deprecated: ")) {
				currentTag.setDeprecated(true);
			}
		}
		if (currentTag!=null && currentTag.getText()!=null){
			HashMap<String, Subtag> subtagsOfType = subtags.get(currentTag.getType());
			if(subtagsOfType == null) {
				subtagsOfType=new HashMap<String, Subtag>();
				subtags.put(currentTag.getType(), subtagsOfType);
			}
			System.out.println(currentTag.getText());
			subtagsOfType.put(currentTag.getText().toLowerCase(), currentTag);
		}
	}

	public boolean containsSubTag(SubtagType type, String subtag) {
		return subtags.get(type).containsKey(subtag.toLowerCase());
	}

	public Subtag getSubTag(SubtagType type, String tag) {
		return subtags.get(type).get(tag.toLowerCase());
	}

	public Collection<Subtag> getAllTags(SubtagType type) {
		return subtags.get(type).values();
	}

	
}
