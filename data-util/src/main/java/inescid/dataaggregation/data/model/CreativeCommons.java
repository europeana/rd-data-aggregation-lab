package inescid.dataaggregation.data.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public final class CreativeCommons {
	public static String PREFIX="cc";
	public static String NS="http://creativecommons.org/ns#";

	public static final Resource License = ResourceFactory.createResource(NS+"License");
//	public static final Property proxyFor = ResourceFactory.createProperty("http://www.openarchives.org/ore/terms/proxyFor");
}
