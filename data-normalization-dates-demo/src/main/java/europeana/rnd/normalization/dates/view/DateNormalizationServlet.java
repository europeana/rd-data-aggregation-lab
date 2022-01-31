package europeana.rnd.normalization.dates.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class DateNormalizationServlet extends HttpServlet {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(DateNormalizationServlet.class);
	
	public enum RequestOperation {
		DISPLAY_START_PAGE, 
		DISPLAY_EUROPEANA_FORM, ANALYSE_EUROPEANA, NORMALIZE_VALUE, DISPLAY_NORMALIZE_FORM;
		
		public static RequestOperation fromHttpRequest(HttpServletRequest req) {
//			System.out.println("req.getPathInfo() " + req.getPathInfo());
//			System.out.println("req.getServletPath() " + req.getServletPath());
			if (req.getPathInfo()!=null) {
				if (req.getPathInfo().endsWith("/check_europeana")) {
					if(!StringUtils.isEmpty(req.getParameter("europeanaID")))
						return RequestOperation.ANALYSE_EUROPEANA;
					return RequestOperation.DISPLAY_EUROPEANA_FORM;
				} else if (req.getPathInfo().endsWith("/normalize_value")) {
					if(!StringUtils.isEmpty(req.getParameter("value")))
						return RequestOperation.NORMALIZE_VALUE;
					return RequestOperation.DISPLAY_NORMALIZE_FORM;					
				}
			}
			return RequestOperation.DISPLAY_START_PAGE;
		}
	};


	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		GlobalUi.init(getInitParameters(config.getServletContext()));
		View.initContext(config.getServletContext().getContextPath());
	}
	
	@Override
	public void destroy() {
		super.destroy();
		try {
			GlobalUi.shutdown();
			System.out.println("Destroying servlet");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private Properties getInitParameters(ServletContext servletContext) {
		Properties props=new Properties();
		Enumeration initParameterNames = servletContext.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			Object pName = initParameterNames.nextElement();
			String initParameter = servletContext.getInitParameter(pName.toString());
			props.setProperty(pName.toString(), initParameter);
		}
		props.setProperty("normalization.webapp.root-folder", servletContext.getRealPath(""));
		return props;
	}
	protected void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			RequestOperation operation = RequestOperation.fromHttpRequest(req);
			System.out.println(operation);
			switch (operation) {
			case ANALYSE_EUROPEANA:{
				DateNormalizationForm form = new DateNormalizationForm(req);
				form.checkUri();
				sendResponse(resp, 200, form.output());
				break;
			} case DISPLAY_EUROPEANA_FORM: {
				DateNormalizationForm form = new DateNormalizationForm();
				sendResponse(resp, 200, form.output());
				break;
			} case DISPLAY_NORMALIZE_FORM: {
				ValueNormalizationForm form = new ValueNormalizationForm();
				sendResponse(resp, 200, form.output());
				break;
			} case NORMALIZE_VALUE: {
				ValueNormalizationForm form = new ValueNormalizationForm(req);
				form.normalize();
				sendResponse(resp, 200, form.output());
				break;
			} case DISPLAY_START_PAGE:
				StartPage form = new StartPage();
				sendResponse(resp, 200, form.output());
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			sendResponse(resp, 500, "Internal error: " + e.getMessage());
		}

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGetOrPost(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGetOrPost(req, resp);
	}


	protected void sendResponse(HttpServletResponse resp, int httpStatus, String body) throws IOException {
		log.info("Response HTTP status: "+ httpStatus);
		resp.setStatus(httpStatus);
		if (body != null && !body.isEmpty()) {
			ServletOutputStream outputStream = resp.getOutputStream();
			outputStream.write(body.getBytes(StandardCharsets.UTF_8));
			resp.setContentType("text/html; charset=utf-8");
		}
	}
}
