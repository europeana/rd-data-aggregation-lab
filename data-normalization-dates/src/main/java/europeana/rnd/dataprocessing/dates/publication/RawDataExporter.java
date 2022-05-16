package europeana.rnd.dataprocessing.dates.publication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import europeana.rnd.dataprocessing.dates.DatesHandler;
import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.dataprocessing.dates.extraction.Match;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.util.RdfUtil.Jena;
import inescid.util.datastruct.MapOfLists;

public class RawDataExporter {
	File exportFolder;
	HashMap<Resource, FileOutputStream> writerByClass;
	HashMap<Resource, FileOutputStream> writerByClassGeneric;

	public RawDataExporter(File exportFolder) {
		super();
		this.exportFolder = exportFolder;
		writerByClass=new HashMap<Resource, FileOutputStream>();
		writerByClassGeneric=new HashMap<Resource, FileOutputStream>();
	}
	
	public void export(DatesInRecord rec) throws FileNotFoundException {
		for(Resource forClass: DatesHandler.edmClassesToProcess) {
			MapOfLists<String, Match> values = rec.fromProvider.getValuesFor(forClass);
			Model model=Jena.createModel();
			Model modelGeneric=Jena.createModel();
			Resource cho = model.createResource(rec.getChoUri());
			for(String propName: values.keySet()) {
				Property prop=propertyFromLocalName(propName);
				for(Match value: values.get(propName)) 
					if(prop.equals(Dc.coverage) || prop.equals(Dc.subject))
						modelGeneric.add(modelGeneric.createStatement(cho, prop, value.getInput()));
					else
						model.add(model.createStatement(cho, prop, value.getInput()));
			}
			if(!model.isEmpty())
				StreamRDFWriter.write(getWriter(forClass),  model.getGraph(), Lang.NTRIPLES);			
			if(!modelGeneric.isEmpty())
				StreamRDFWriter.write(getWriterGeneric(forClass),  modelGeneric.getGraph(), Lang.NTRIPLES);			
		}
	}

	private Property propertyFromLocalName(String propName) {
		for(Property prop: DatesHandler.temporalProperties) {
			if(prop.getLocalName().equals(propName))
				return prop;
		}
		throw new IllegalArgumentException(propName);
	}

	public void close() throws IOException {
		for(FileOutputStream out: writerByClass.values())
			out.close();
		for(FileOutputStream out: writerByClassGeneric.values())
			out.close();
	}
	
	private FileOutputStream getWriter(Resource forClass) throws FileNotFoundException {
		FileOutputStream w = writerByClass.get(forClass);
		if(w==null) {
			w=new FileOutputStream(new File(exportFolder, "temporal-"+Edm.getPrefixedName(forClass).replace(':', '_')+".nt"));
			writerByClass.put(forClass, w);
		}
		return w;
	}
	
	private FileOutputStream getWriterGeneric(Resource forClass) throws FileNotFoundException {
		FileOutputStream w = writerByClassGeneric.get(forClass);
		if(w==null) {
			w=new FileOutputStream(new File(exportFolder, "generic-"+Edm.getPrefixedName(forClass).replace(':', '_')+".nt"));
			writerByClassGeneric.put(forClass, w);
		}
		return w;
	}
	
}
