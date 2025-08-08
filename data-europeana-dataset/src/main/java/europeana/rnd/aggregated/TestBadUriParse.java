/**
 * 
 */
package europeana.rnd.aggregated;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
//import org.apache.jena.riot.system.IRIx;
//import org.apache.jena.riot.system.IRILang;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.RiotLib;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class TestBadUriParse {

    public static void main(String[] args) {
        String rdfDataWithBadUri = "@prefix ex: <http://example.org/> .\n" +
                                   "<http://example.org/my bad uri> ex:prop \"value\" .";

        List<String> collectedErrors = new ArrayList<>();

        // Custom ErrorHandler that collects errors instead of throwing exceptions
        ErrorHandler customErrorHandler = new ErrorHandler() {
            @Override
            public void warning(String message, long line, long col) {
                collectedErrors.add(String.format("WARNING: %s (Line: %d, Col: %d)", message, line, col));
            }

            @Override
            public void error(String message, long line, long col) {
                collectedErrors.add(String.format("ERROR: %s (Line: %d, Col: %d)", message, line, col));
                // You could choose to throw an exception here, or continue parsing
                // For "bad URI" parsing, you might want to continue.
            }

            @Override
            public void fatal(String message, long line, long col) {
                collectedErrors.add(String.format("FATAL: %s (Line: %d, Col: %d)", message, line, col));
                throw new RuntimeException(String.format("Fatal parsing error: %s", message));
            }
        };

        Model model = ModelFactory.createDefaultModel();

        try {
            RDFParser.create()
                    .source(new StringReader(rdfDataWithBadUri))
                    .lang(org.apache.jena.riot.Lang.TURTLE)
//                    .errorHandler(customErrorHandler) // Set your custom error handler
                    // .strict(false) // This is generally for less strict syntax, not necessarily bad URIs
                    .parse(StreamRDFLib.graph(model.getGraph()));

            System.out.println("Parsing attempted.");
            if (!collectedErrors.isEmpty()) {
                System.out.println("\nErrors and Warnings encountered:");
                for (String error : collectedErrors) {
                    System.out.println(error);
                }
            } else {
                System.out.println("No errors or warnings reported by the custom handler.");
            }

            System.out.println("\nModel content (if any triples were parsed):");
            model.write(System.out, "TURTLE");

        } catch (Exception e) {
            System.err.println("An exception occurred during parsing: " );
            e.printStackTrace();
        }
    }
}
