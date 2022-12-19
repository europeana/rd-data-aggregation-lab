package inesc.util.testing;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;

import inescid.util.RdfUtil;

public class TestRdfUri {

	public static void main(String[] args) throws Exception {
		Resource res = RdfUtil.readRdfResourceFromUri("http://data.europeana.eu/item/2024904/https___www_topfoto_co_uk_Europeana_EuropeanKaleidoscope_1200px_0038812_jpg");
		RdfUtil.printOutRdf(res.getModel());
	}
}
