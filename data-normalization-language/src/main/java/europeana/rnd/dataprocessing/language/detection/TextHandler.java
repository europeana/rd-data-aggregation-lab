package europeana.rnd.dataprocessing.language.detection;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import europeana.rnd.dataprocessing.language.Source;
import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.RdaGr2;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil;

public class TextHandler {
	
//record URI:
//	class property tagvalue count
	Set<Property> propertiesToExtract=new HashSet<Property>() {{
		add(Dc.title);
		add(Dc.description);
	}};

	Pattern hasTextPattern=Pattern.compile("\\p{L}.*\\p{L}.*\\p{L}");
	
	public static void main(String[] args) throws Exception {
//		Pattern hasTextPattern=Pattern.compile("\\p{Alpha}.*\\p{Alpha}.*\\p{Alpha}");
		Pattern hasTextPattern=Pattern.compile("\\p{L}.*\\p{L}.*\\p{L}");
		for (String txt: new String[] {"1" , "as4r","t肖晗","t3á2晗"}) {
			System.out.print(txt+" - ");
			System.out.println( hasTextPattern.matcher(txt).find() );
			
		}
	}
	
	TextJsonWriter jsonWriter;
	
	public TextHandler(String outputFolder) throws IOException {
		super();
		this.jsonWriter = new TextJsonWriter(outputFolder);
	}

	public void handle(Model edm, int recCnt) throws IOException {
		TextInRecord res = getTextInRecord(edm);
		jsonWriter.write(res); 
	}
	
//	public static TextInRecord getDatesInRecordSingleRecord(Model edm) {
//		return getLanguageInRecord(edm, new EntityTrackerOnMemory(Source.EUROPEANA));
//	}

	private TextInRecord getTextInRecord(Model edm) {
		HashSet<Resource> processedResources=new HashSet<Resource>();
		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		String choUri = choRes.getURI();
		TextInRecord res = new TextInRecord(choUri);
		StmtIterator listStatements = edm.listStatements(null, Edm.language, (RDFNode) null);
		if(listStatements.hasNext()) {
			res.setEdmLanguage(listStatements.next().getObject().asLiteral().getString());
			listStatements.close();
		}
		for (Resource proxy : edm.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList()) {
			Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
			Source isEuropeanaProxy = europeanaProxySt != null
					&& europeanaProxySt.getObject().asLiteral().getBoolean() ? Source.EUROPEANA : Source.PROVIDER;
			if(isEuropeanaProxy==Source.EUROPEANA)
				continue;
			for (Statement st : proxy.listProperties().toList()) {
//				if (st.getObject().isLiteral() && propertiesToExtract.contains(st.getPredicate())) {
//					res.addText(st.getPredicate(), st.getObject().asLiteral());
//				} 
				
//				if (st.getObject().isLiteral() ) {
//					System.out.println(st.getObject().asLiteral().getDatatype().getClass());
//					System.out.println(st.getObject().asLiteral().getDatatype().getURI());					
//				}
				if (st.getObject().isLiteral() && st.getObject().asLiteral().getDatatype().getURI().endsWith("tring") && isText(st.getObject().asLiteral().getString())){
					res.addText(st.getPredicate(), st.getObject().asLiteral());
				}				
			}
		}
		return res;
	}


	private boolean isUrl(String string) {
		return (string.startsWith("http:/") || string.startsWith("https:/")) && string.indexOf(' ')<0;
	}
	
	
	private boolean isText(String val) {
		return hasTextPattern.matcher(val).find() &&  
				!((val.startsWith("http:/") || val.startsWith("https:/")) && val.indexOf(' ')<0);
	}
	

	public void finalize() throws IOException {
		jsonWriter.close();
	}
}