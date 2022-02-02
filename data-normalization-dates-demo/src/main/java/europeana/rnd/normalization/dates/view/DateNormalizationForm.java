package europeana.rnd.normalization.dates.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import europeana.rnd.dataprocessing.dates.DatesExtractorHandler;
import europeana.rnd.dataprocessing.dates.DatesHandler;
import europeana.rnd.dataprocessing.dates.DatesInRecord;
import europeana.rnd.normalization.dates.view.ResourceView.DataModel;
import inescid.dataaggregation.data.model.Edm;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

public class DateNormalizationForm extends UriForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(DateNormalizationForm.class);

	String operation=null;
	boolean uriChecked=false;
	
	DateNormalizationView cho;
	Resource edmChoRes;
	
	public DateNormalizationForm() {
	} 

	public DateNormalizationForm(HttpServletRequest req) {
		super(req.getParameter("europeanaID"));
		operation=req.getParameter("operation");
	}

	private void loadAndExtractDataFromUri() {
		String rdfXml = null;
		try {
			String canonicalUri = convertToCanonical(uri);
			rdfXml = HttpUtil.makeRequestForContent(canonicalUri, "Accept", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			System.out.println(rdfXml);
			
			if(rdfXml!=null) {
				Model edm = RdfUtil.readRdf(rdfXml, Lang.RDFXML);
	//			String edmXml=HttpUtil.makeRequest(uri, "Accept", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER).getResponseContentAsString();
	////			TiersCalculation calculate = EpfTiersCalculator.calculateOnInternalEdm(edm.createResource(uri)); 
	//			TiersCalculation calculate = EpfTiersCalculator.calculate(edmXml); 
				if(edm!=null) {
					edmChoRes= edm.createResource(canonicalUri);
					if(edmChoRes==null) 
						message="CHO not found: "+ uri;
				} else {
					message="Could not find Europeana record: "+ uri;
				}
			} else {
				message="Could not find Europeana record: "+ uri;
			}
		} catch (RiotException e) {
			message="Invalid response received from Europeana: "+ e.getMessage()+" : "+ uri ;
			e.printStackTrace();
		} catch (IOException e) {
			message="Problem accessing Europeana: "+ e.getMessage()+" : "+ uri ;
			e.printStackTrace();
		} catch (InterruptedException e) {
			message="Unkown problem accessing Europeana: "+ uri;
			e.printStackTrace();
		} catch (AccessException e) {
			message="Could not find Europeana record: "+ uri;
			e.printStackTrace();
		}
	}

	public void checkUri() {
		uriChecked=true;
		message=validateUri();
		if(message!=null)
			return;
		loadAndExtractDataFromUri();
		if(edmChoRes!=null) {
			try {
				//TODO execute date normalization
				DatesInRecord datesInRecord = DatesHandler.getDatesInRecord(edmChoRes.getModel());
				DatesExtractorHandler.runDateNormalization(datesInRecord);
				cho=new DateNormalizationView(datesInRecord);		
			} catch (Exception e) {
				message=e.getMessage();
				e.printStackTrace();
			}
		}
	}

	public boolean isUriChecked() {
		return uriChecked;
	}
	
	@Override
	public String validateUri() {
		if(StringUtils.isEmpty(uri)) {
			return "Provide the Europeana ID or URI";
		}
		return null;
	}

	public static String convertToCanonical(String idOrUri) {
		if(!idOrUri.startsWith("http://")) 
			idOrUri="http://data.europeana.eu/item"+
					(idOrUri.startsWith("/") ? "" : "/") +
					idOrUri;
		else if(! idOrUri.startsWith("http://data.europeana.eu/")) { 
			if(idOrUri.contains(".europeana.eu/")) {
				String[] split = idOrUri.split("/");
				if(split.length>=3) 
					idOrUri="http://data.europeana.eu/item"+split[split.length-2]+"/"+split[split.length-1];
			}
		}
		return idOrUri;
	}

	public DateNormalizationView getNormalizationResult() {
		return cho;
	}
}
