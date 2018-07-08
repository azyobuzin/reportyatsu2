package reportyatsu2;

import java.io.InputStream;
import java.net.URL;

final class Resources {
    private Resources() {}

    public static final String REPORT_SCHEMA_DEFINITION = "report.xsd";
    public static final String DEFAULT_STYLES = "styles.xml";
    public static final String CONTENT_TEMPLATE = "contentTemplate.xml";

    public static URL getResourceUrl(String resourceName) {
        return Resources.class.getResource(resourceName);
    }

    public static InputStream getResourceStream(String resourceName) {
        return Resources.class.getResourceAsStream(resourceName);
    }
}
