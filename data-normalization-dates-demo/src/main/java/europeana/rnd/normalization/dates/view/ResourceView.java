package europeana.rnd.normalization.dates.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.util.RdfUtil;

public class ResourceView {
	public enum DataModel {
		Schemaorg, SchemaorgPlusEdm, ALL
	};

	Resource res;
	DataModel datamodel;
	
	public ResourceView(Resource st, DataModel datamodel) {
		super();
		this.res = st;
		this.datamodel = datamodel;
	}

	public String getUri() {
		return RdfUtil.getUriOrId(res);
	}

	public List<StatementView> getTypes() {
		List<StatementView> sts=new ArrayList<StatementView>();
		for(Statement st: res.listProperties(Rdf.type).toList()) {
			if(datamodel==DataModel.SchemaorgPlusEdm) {
				if(!st.getObject().isURIResource())
					continue;
				if(st.getObject().asResource().getURI().startsWith(Schemaorg.NS) || st.getObject().asResource().getURI().startsWith(Rdf.NS)) {
					sts.add(new StatementView(st));
					continue;
				}					
				for(String ns: Edm.NS_EXTERNAL_PREFERRED_BY_NS.keySet())
					if(st.getObject().asResource().getURI().startsWith(ns) ) {
						sts.add(new StatementView(st));
						continue;
					}					
			} else if(datamodel==DataModel.Schemaorg) {
				if(!st.getObject().isURIResource()
						|| (!st.getObject().asResource().getURI().startsWith(Schemaorg.NS) && !st.getObject().asResource().getURI().startsWith(Rdf.NS))) 
					continue;
				sts.add(new StatementView(st));					
			} else { //aLL
				sts.add(new StatementView(st));										
			}
		}
		return sts;
	}

	public List<StatementView> getStatements() {
		List<StatementView> sts = new ArrayList<StatementView>();
		for (Statement st : res.listProperties().toList()) {
			if (st.getPredicate().equals(Rdf.type))
				continue;
			if(datamodel==DataModel.SchemaorgPlusEdm) {
				if (st.getPredicate().getURI().startsWith(Schemaorg.NS) || st.getPredicate().getURI().startsWith(Rdf.NS)) {
					sts.add(new StatementView(st));
					continue;
				}
				for(String ns: Edm.NS_EXTERNAL_PREFERRED_BY_NS.keySet())
					if(st.getPredicate().getURI().startsWith(ns) ) {
						sts.add(new StatementView(st));
						break;
					}
				sts.add(new StatementView(st));
			} else if(datamodel==DataModel.Schemaorg) {
				if (!st.getPredicate().getURI().startsWith(Schemaorg.NS) && !st.getPredicate().getURI().startsWith(Rdf.NS))
					continue;
				sts.add(new StatementView(st));			
			} else { //aLL
				sts.add(new StatementView(st));			
			}
		}
		
		Collections.sort(sts, new Comparator<StatementView>() {
			@Override
			public int compare(StatementView a, StatementView b) {
				return a.getPredicate().compareTo(b.getPredicate());
			}
		});
		return sts;
	}

	public boolean isCreativeWork() {
		for (Statement st : res.listProperties(Rdf.type).toList()) {
			if (!st.getObject().isURIResource())
				continue;
			if (Schemaorg.creativeWorkClasses.contains(st.getObject().asResource()))
				return true;
			if (Edm.ProvidedCHO.equals(st.getObject().asResource()))
				return true;
		}
		return false;
	}

	public boolean hasProperties() {
		for (Statement st : res.listProperties().toList()) {
			if (st.getPredicate().equals(Rdf.type))
				return true;
			if (!st.getPredicate().getURI().startsWith(Schemaorg.NS) && !st.getPredicate().getURI().startsWith(Rdf.NS))
				continue;
			return true;
		}
		return false;
	}

	public Resource getResource() {
		return res;
	}


	public Model getModel() {
		return res.getModel();
	}

}