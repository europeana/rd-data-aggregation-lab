package europeana.rnd.dataprocessing.uri;

import java.io.IOException;
import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Rdf;
import inescid.util.RdfUtil;

public class EntityUriHandler {
	
//record URI:
//	class property tagvalue count
	
	UrisJsonWriter jsonWriter;
//	EntityTracker entityTracker=new EntityTracker();
	
	public EntityUriHandler(String outputFolder) throws IOException {
		super();
		this.jsonWriter = new UrisJsonWriter(outputFolder);
	}

	public void handle(Model edm, int recCnt) throws IOException {
		UrisInRecord res = getUrisInRecord(edm);
		jsonWriter.write(res); 
	}
	
	public static UrisInRecord getDatesInRecordSingleRecord(Model edm) {
		return getUrisInRecord(edm);
	}

	private static UrisInRecord getUrisInRecord(Model edm) {
		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		String choUri = choRes.getURI();
		
		UrisInRecord res = new UrisInRecord(choUri);
		for (Resource proxy : edm.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList()) {
			Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
			Source isEuropeanaProxy = europeanaProxySt != null
					&& europeanaProxySt.getObject().asLiteral().getBoolean() ? Source.EUROPEANA : Source.PROVIDER;
			for (Statement st : proxy.listProperties().toList()) {
				if(st.getObject().isResource()) {
					res.addTo(isEuropeanaProxy, st.getObject().asResource(), st.getPredicate());
				}
			}
		}
		return res;
	}

	public void finalize() throws IOException {
		jsonWriter.close();
	}
}