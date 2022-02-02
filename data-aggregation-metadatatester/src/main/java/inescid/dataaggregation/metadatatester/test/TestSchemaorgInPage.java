package inescid.dataaggregation.metadatatester.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.ExtractorGroup;
import org.apache.any23.extractor.html.EmbeddedJSONLDExtractorFactory;
import org.apache.any23.extractor.rdfa.RDFa11ExtractorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.metadatatester.view.JenaTripleHandler;
import inescid.dataaggregation.metadatatester.view.ResourceView;
import inescid.dataaggregation.metadatatester.view.ResourceView.DataModel;
import inescid.http.HttpRequest;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class TestSchemaorgInPage {

	public static void main(String[] args) throws Exception {
		Global.init_componentHttpRequestService();
		
		String uri="http://www.museumap.hu/record/-/record/oai-aggregated-bib4218550";
		final Any23 any23=new Any23(new ExtractorGroup(new ArrayList() {{ 
			add(new EmbeddedJSONLDExtractorFactory());
			add(new RDFa11ExtractorFactory());
//				add(new MicrodataExtractorFactory());
//				add(new HTMLMetaExtractorFactory());
		}}));
		try {				
			HttpRequest request = new HttpRequest(uri);
			request.fetch();
			if(request.getResponseStatusCode()==200) {
				Model model=Jena.createModel();
				any23.extract(request.getResponseContentAsString(), request.getUrl(), request.getMimeType(), 
						request.getCharset()!=null ? request.getCharset().name() : StandardCharsets.UTF_8.name(),
								new JenaTripleHandler(model));
				
				RdfUtil.printOutRdf(model);
			} else {
				System.out.println("Could not access URL (HTTP statos code: "+request.getResponseStatusCode()+")");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExtractionException e) {
			e.printStackTrace();
		}
	}
}
