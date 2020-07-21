package inescid.dataaggregation.casestudies.edm.alignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;

import inescid.util.datastruct.HashMapWithFactory;
import inescid.util.datastruct.MapOfLists;

public class Alignment {
	Map<String, ClassAlignment> alignedClasses=new HashMapWithFactory<String, ClassAlignment>(ClassAlignment.class);
	MapOfLists<String, ClassAlignment> alignedClassesIndex;

	public void init() {
		alignedClassesIndex=new MapOfLists<String, ClassAlignment>();
		for(ClassAlignment clsAlig: alignedClasses.values()) {
			for(String alignedWith:clsAlig.getAlignedWith()) 
				alignedClassesIndex.put(alignedWith, clsAlig);
		}
	}
	
	
	public List<ClassAlignment> getAlignmentsFor(String extClassUri) {
		ArrayList<ClassAlignment> aligs = alignedClassesIndex.get(extClassUri);
		return aligs == null ? Collections.EMPTY_LIST : aligs;
	}

	public ClassAlignment addClass(String uri) {
		ClassAlignment newAlig = new ClassAlignment();
		alignedClasses.put(uri, newAlig);
		return newAlig;
	}


	public ClassAlignment getClassAlignment(String clsUri) {
		return alignedClasses.get(clsUri);		
	}
	public ClassAlignment getClassAlignment(Resource cls) {
		return alignedClasses.get(cls.getURI());
	}
	
	
}
