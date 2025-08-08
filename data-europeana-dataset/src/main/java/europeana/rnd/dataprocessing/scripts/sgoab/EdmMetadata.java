package europeana.rnd.dataprocessing.scripts.sgoab;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.Edm;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

class EdmMetadata {
	Model edm;
	public EdmMetadata(String choUri) throws AccessException, InterruptedException, IOException {
		System.out.println(choUri);
		edm = RdfUtil.readRdfFromUri(choUri);
	}
	
	public List<String[]> getSubjects() {
		List<String[]> ret=new ArrayList<String[]>();
		StmtIterator subjects = EdmRdfUtil.listPropertyOfProxy(edm, Dc.subject, false);
		for(Statement st: subjects.toList()) {
			String sub=RdfUtil.getUriOrLiteralValue(st.getObject());
			if(sub.toLowerCase().startsWith("http://iconclass.org/")) 
				sub="https"+sub.substring(4);
			if(sub.toLowerCase().startsWith("https://iconclass.org/")) {
				String label=sub.substring("http://iconclass.org/".length()+1);
				try {
					Model model = RdfUtil.readRdfFromUri(sub+".rdf");
					Resource subjectRes = model.getResource(sub);
					if(subjectRes!=null) {
						Literal labelLit = RdfUtil.getLabelForResource(subjectRes, "en");
						if(labelLit!=null) 
							label=labelLit.getString();
					}
				} catch (Exception e) {
					System.err.println(sub);
					e.printStackTrace();
				}
				ret.add(new String[] {sub, label});						
			}
		}
		return ret;
	}
	
	public String getImageUrl() {
		RDFNode edmObj = EdmRdfUtil.getPropertyOfAggregation(edm, Edm.isShownBy);
		return RdfUtil.getUriOrLiteralValue(edmObj);
	}
}