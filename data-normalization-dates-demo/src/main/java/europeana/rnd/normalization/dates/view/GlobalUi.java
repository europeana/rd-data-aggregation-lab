package europeana.rnd.normalization.dates.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import inescid.http.HttpRequestService;
import inescid.util.AccessException;

public class GlobalUi {
//	public static Pattern urlPattern=Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
//	public static final Charset UTF8 = Charset.forName("UTF8");
	public static final Configuration FREE_MARKER=new Configuration(Configuration.VERSION_2_3_27);
//	public static final String SEE_ALSO_DATASET_PREFIX = "seeAlso_"; 
//	public static final String CONVERTED_EDM_DATASET_PREFIX = "convertedEdm_"; 
//	public static String GOOGLE_API_CREDENTIALS = ""; 
	
	private static File webappRoot=null;
	
	
	static {
		GlobalUi.FREE_MARKER.setClassLoaderForTemplateLoading(DateNormalizationServlet.class.getClassLoader(), "europeana/rnd/normalization/dates/view/template");
		GlobalUi.FREE_MARKER.setDefaultEncoding(StandardCharsets.UTF_8.toString());
		GlobalUi.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		GlobalUi.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);	
	}
	public static synchronized void init(Properties prop) {
		if(webappRoot==null) {
			System.out.println("Normalization webapp initializing");
			String webappRootPath = prop.getProperty("normalization.webapp.root-folder");
			webappRoot=new File(webappRootPath);
		}
	}
	public static synchronized void shutdown() throws IOException {
		webappRoot=null;
	}

	public static synchronized void init_developement() {
		Properties props=new Properties();
		props.setProperty("normalization.webapp.root-folder", "C:\\Users\\nfrei\\workspace-eclipse\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\data-aggregation-metadatatester");
		init(props);
	}
	


//	private static void initLogging() {
//        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//        final org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
//        Layout layout = PatternLayout.createLayout(PatternLayout.SIMPLE_CONVERSION_PATTERN, config, null,
//            null,null, null);
//        Appender appender = FileAppender.createAppender("target/test.log", "false", "false", "File", "true",
//            "false", "false", "4000", layout, null, "false", null, config);
//        appender.start();
//        config.addAppender(appender);
//        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
//        AppenderRef[] refs = new AppenderRef[] {ref};
//        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", "info", "org.apache.logging.log4j",
//            "true", refs, null, config, null );
//        loggerConfig.addAppender(appender, null, null);
//        config.addLogger("org.apache.logging.log4j", loggerConfig);
//        ctx.updateLoggers();
//	}
}
