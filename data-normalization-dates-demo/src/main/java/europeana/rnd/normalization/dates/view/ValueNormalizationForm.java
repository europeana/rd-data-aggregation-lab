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
import europeana.rnd.dataprocessing.dates.extraction.Match;
import europeana.rnd.normalization.dates.view.ResourceView.DataModel;
import inescid.dataaggregation.data.model.Edm;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

public class ValueNormalizationForm extends View {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(ValueNormalizationForm.class);

	protected Match match=null;
	
	public ValueNormalizationForm() {
		match=new Match("");
	} 

	public ValueNormalizationForm(HttpServletRequest req) {
		match=new Match(req.getParameter("value"));		
	}

	public Match getNormalizationResult() {
		return match;
	}

	public void normalize() {
		try {
			match=DatesExtractorHandler.runDateNormalization(match.getInput());
			System.out.println(match.getMatchId());
		} catch (Exception e) {
			message=e.getMessage();
			e.printStackTrace();
		}
	}

	public Match getMatch() {
		return match;
	}
	
}
