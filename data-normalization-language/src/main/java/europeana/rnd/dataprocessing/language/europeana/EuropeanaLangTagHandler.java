package europeana.rnd.dataprocessing.language.europeana;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Literal;
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
import europeana.rnd.dataprocessing.language.EntityTracker;
import europeana.rnd.dataprocessing.language.EntityTrackerOnDisk;
import europeana.rnd.dataprocessing.language.EntityTrackerOnMemory;
import europeana.rnd.dataprocessing.language.Source;
import europeana.rnd.dataprocessing.language.etranslate.NormalisableTags;
import europeana.rnd.dataprocessing.language.etranslate.NotNormalizableTags;

public class EuropeanaLangTagHandler {
	EuropeanaLangTagValueWriter valueWriter;
	
	public EuropeanaLangTagHandler(String outputFolder) throws IOException {
		super();
		this.valueWriter = new EuropeanaLangTagValueWriter(outputFolder);
	}

	public void handle(Model edm, int recCnt) throws IOException {
		HashSet<Resource> processedResources=new HashSet<Resource>();
		Resource choRes = RdfUtil.findFirstResourceWithProperties(edm, Rdf.type, Edm.ProvidedCHO, null, null);
		
		for (Resource proxy : edm.listResourcesWithProperty(Rdf.type, Ore.Proxy).toList()) {
			Statement europeanaProxySt = proxy.getProperty(Edm.europeanaProxy);
			Source source = europeanaProxySt != null
					&& europeanaProxySt.getObject().asLiteral().getBoolean() ? Source.EUROPEANA : Source.PROVIDER;
			if(source!=Source.EUROPEANA)
				continue;
			for (Statement st : proxy.listProperties().toList()) {
				if (st.getObject().isLiteral()) {
					testLiteral(choRes.getURI(), source, Ore.Proxy, st);
				} else if(st.getObject().isResource()) {
					Statement typeSt = st.getObject().asResource().getProperty(Rdf.type);
					if(typeSt!=null && typeSt.getObject().isResource()) {
						Resource resType=typeSt.getObject().asResource();
//						System.out.println(resType);
						getLanguageInResource(choRes.getURI(), source, resType, st.getObject().asResource(),processedResources);
					}						
				}
			}
		}
		
//		for (Resource webRes : edm.listResourcesWithProperty(Rdf.type, Edm.WebResource).toList()) {
//			boolean isFullText=false;
//			for (Statement st : webRes.listProperties(Rdf.type).toList()) {
//				if(st.getObject().equals(Edm.FullTextResource)) {
//					isFullText=true;
//					break;
//				}
//			}
//			if(!isFullText) 
//				continue;
//			for (Statement st : webRes.listProperties().toList()) {
//				if (st.getObject().isLiteral()) {
//					testLiteral(choRes.getURI(), Source.EUROPEANA, Edm.WebResource, st);
//				} else if(st.getObject().isResource()) {
//					Statement typeSt = st.getObject().asResource().getProperty(Rdf.type);
//					if(typeSt!=null && typeSt.getObject().isResource()) {
//						Resource resType=typeSt.getObject().asResource();
//						getLanguageInResource(choRes.getURI(), Source.EUROPEANA, resType, st.getObject().asResource(),processedResources);
//					}						
//				}
//			}
//		}
	}

	private void getLanguageInResource(String choUri, Source source, Resource resType, Resource resource, HashSet<Resource> processedResources) throws IOException {
		if(resType.equals(Ore.Proxy) || resType.equals(Edm.ProvidedCHO) || resType.equals(Edm.WebResource) || resType.equals(Ore.Aggregation) || resType.equals(Edm.EuropeanaAggregation) ) 
			return;
		if(processedResources.contains(resource))
			return;
		processedResources.add(resource);
//		if(resource.isURIResource()) {
//			if(entityTracker.contains(source, resource.getURI()))
//				return;
//			entityTracker.add(source, resource.getURI());
//		}
		for (Statement st : resource.listProperties().toList()) {
			if (st.getObject().isLiteral()) {
				testLiteral(choUri, source, resType, st);
			} else if(st.getObject().isResource()) {
				Statement typeSt = st.getObject().asResource().getProperty(Rdf.type);
				if(typeSt!=null && typeSt.getObject().isResource()) {
					Resource subResType=typeSt.getObject().asResource();
					getLanguageInResource(choUri, source, subResType, st.getObject().asResource(), processedResources);
				}
			}
		}
	}

	
	private void testLiteral(String choUri, Source source, Resource rdfType, Statement st) throws IOException {
		Literal lit=st.getObject().asLiteral();
		if(!StringUtils.isEmpty(lit.getLanguage()) && (NotNormalizableTags.TAG_SET.contains(lit.getLanguage()) || NormalisableTags.TAG_MAP.containsKey(lit.getLanguage()))) {
//			String rdfType;
//			
//			Statement typeSt = st.getSubject().getProperty(Rdf.type);
//			if(typeSt==null)
//				rdfType="";
//			else
//				rdfType=typeSt.getObject().asResource().getURI();
			

			String subjectUri=st.getSubject().isURIResource() ? st.getSubject().getURI() : "anonymous"; 
			
			
			valueWriter.write(choUri, source, subjectUri, rdfType.getURI(),  Edm.getPrefixedName(st.getPredicate()), lit.getLanguage(), lit.getString());
		}
	}
	
	
	public void finalize() throws IOException {
		valueWriter.close();
	}
}