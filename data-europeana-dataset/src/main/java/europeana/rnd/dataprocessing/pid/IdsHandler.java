package europeana.rnd.dataprocessing.pid;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Dc;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.RdaGr2;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil;

public class IdsHandler {
	
	IdsJsonWriter jsonWriter;
	
	public IdsHandler(String outputFolder) throws IOException {
		super();
		this.jsonWriter = new IdsJsonWriter(outputFolder);
	}

	public void handle(Model edm, int recCnt) throws IOException {
		IdsInRecord res = getIdsInRecord(edm);
		jsonWriter.write(res); 
	}
	
	public static IdsInRecord getIdsInRecord(Model edm) {
		HashSet<Resource> processedResources=new HashSet<Resource>();
		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		String choUri = choRes.getURI();
		
		IdsInRecord res = new IdsInRecord(choUri);
		for (Resource proxy : edm.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList()) {
			Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
			boolean isEuropeanaProxy = europeanaProxySt != null	&& europeanaProxySt.getObject().asLiteral().getBoolean();
			if (isEuropeanaProxy)
				continue;
			for (Statement st : proxy.listProperties(Dc.identifier).toList()) {
				res.addTo(Ore.Proxy, st.getPredicate(), RdfUtil.getUriOrLiteralValue(st.getObject()));
			}
		}
		
		for (Resource webRes : edm.listResourcesWithProperty(Rdf.type, Ore.Aggregation).toList()) {
			for (Statement st : webRes.listProperties(Edm.isShownBy).toList() )  
				res.addTo(Ore.Aggregation, st.getPredicate(), RdfUtil.getUriOrLiteralValue(st.getObject()));
			for (Statement st : webRes.listProperties(Edm.isShownAt).toList()) 
				res.addTo(Ore.Aggregation, st.getPredicate(), RdfUtil.getUriOrLiteralValue(st.getObject()));
		}
		
		for (Resource webRes : edm.listResourcesWithProperty(Rdf.type, Edm.WebResource).toList()) {
			for (Statement st : webRes.listProperties(Dc.identifier).toList()) {
				res.addTo(Edm.WebResource, st.getPredicate(), RdfUtil.getUriOrLiteralValue(st.getObject()));
			}
		}
		
		return res;
	}


	public void finalize() throws IOException {
		jsonWriter.close();
	}
}