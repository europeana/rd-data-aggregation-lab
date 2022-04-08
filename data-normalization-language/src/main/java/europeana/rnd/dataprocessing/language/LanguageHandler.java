package europeana.rnd.dataprocessing.language;

import java.io.File;
import java.io.IOException;
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

public class LanguageHandler {
	
//record URI:
//	class property tagvalue count
	
	LanguageJsonWriter jsonWriter;
	EntityTrackerOnDisk entityTracker;
//	EntityTracker entityTracker=new EntityTracker();
	
	public LanguageHandler(String outputFolder) throws IOException {
		super();
		this.jsonWriter = new LanguageJsonWriter(outputFolder);
		File setFolder = new File(outputFolder, "entity-tracker");
		if (setFolder.exists()) 
			FileUtils.deleteDirectory(setFolder);
		setFolder.mkdir();
		entityTracker=new EntityTrackerOnDisk(setFolder);
	}

	public void handle(Model edm, int recCnt) throws IOException {
		LanguageInRecord res = getLanguageInRecord(edm, entityTracker);
		jsonWriter.write(res); 
	}
	
	public static LanguageInRecord getDatesInRecordSingleRecord(Model edm) {
		return getLanguageInRecord(edm, new EntityTrackerOnMemory());
	}

	private static LanguageInRecord getLanguageInRecord(Model edm, EntityTracker entityTracker) {
//			HashSet<String> processedResources=new HashSet<String>();
		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		String choUri = choRes.getURI();
		
		LanguageInRecord res = new LanguageInRecord(choUri);
		for (Resource proxy : edm.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList()) {
			Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
			Source isEuropeanaProxy = europeanaProxySt != null
					&& europeanaProxySt.getObject().asLiteral().getBoolean() ? Source.EUROPEANA : Source.PROVIDER;
			for (Statement st : proxy.listProperties().toList()) {
				if (st.getObject().isLiteral()) {
					res.addTo(isEuropeanaProxy, Ore.Proxy, st.getPredicate(), st.getObject().asLiteral());
				} else if(st.getObject().isResource()) {
					Statement typeSt = st.getObject().asResource().getProperty(Rdf.type);
					if(typeSt!=null && typeSt.getObject().isResource()) {
						Resource resType=typeSt.getObject().asResource();
						getLanguageInResource(res, isEuropeanaProxy, resType, st.getObject().asResource(), entityTracker);
					}						
				}
			}
		}
		for (Resource webRes : edm.listResourcesWithProperty(Rdf.type, Edm.WebResource).toList()) {
			for (Statement st : webRes.listProperties().toList()) {
				if (st.getObject().isLiteral()) {
					res.addTo(Source.PROVIDER, Edm.WebResource, st.getPredicate(), st.getObject().asLiteral());
				} else if(st.getObject().isResource()) {
					Statement typeSt = st.getObject().asResource().getProperty(Rdf.type);
					if(typeSt!=null && typeSt.getObject().isResource()) {
						Resource resType=typeSt.getObject().asResource();
						getLanguageInResource(res, Source.PROVIDER, resType, st.getObject().asResource(), entityTracker);
					}						
				}
			}
		}
		return res;
	}

	private static  void getLanguageInResource(LanguageInRecord res, Source source, Resource resType, Resource resource, EntityTracker entityTracker) {
		if(resType.equals(Ore.Proxy) || resType.equals(Edm.ProvidedCHO) || resType.equals(Edm.WebResource)) 
			return;
		if(resource.isURIResource()) {
			if(entityTracker.contains(source, resource.getURI()))
				return;
			entityTracker.add(source, resource.getURI());
		}
		for (Statement st : resource.listProperties().toList()) {
			if (st.getObject().isLiteral()) {
				res.addTo(source, resType, st.getPredicate(), st.getObject().asLiteral());
			} else if(st.getObject().isResource()) {
				Statement typeSt = st.getObject().asResource().getProperty(Rdf.type);
				if(typeSt!=null && typeSt.getObject().isResource()) {
					Resource subResType=typeSt.getObject().asResource();
					getLanguageInResource(res, source, subResType, st.getObject().asResource(), entityTracker);
				}
			}
		}
	}

	public void finalize() throws IOException {
		jsonWriter.close();
	}
}