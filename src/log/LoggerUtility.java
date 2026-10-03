package log;

import java.util.Properties;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Utility class used to generate Log4j logger.
 * We can generate logs in a text or a html file.
 *
 * @author Tianxiao.Liu@u-cergy.fr
 */
public class LoggerUtility {

	public static Logger getLogger(Class<?> logClass, String logFileType) {
		try {
			String configFile = logFileType.equals("html")
				? "log/log4j-html.properties"
				: "log/log4j-text.properties";
			InputStream is = LoggerUtility.class.getClassLoader()
				.getResourceAsStream(configFile);
			if (is != null) {
				Properties props = new Properties();
				props.load(is);
				PropertyConfigurator.configure(props);
			}
		} catch (Exception e) {
			System.err.println("log4j config error: " + e.getMessage());
		}
		return Logger.getLogger(logClass.getName());
	}
}
