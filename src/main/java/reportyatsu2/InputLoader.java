package reportyatsu2;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InputLoader {
    public Document loadToDom(InputStream stream) throws InputXmlException, IOException {
        DocumentBuilder builder = createDocumentBuilder();
        InputHandler handler = new InputHandler();
        builder.setErrorHandler(handler);

        Document document;
        try {
            document = builder.parse(stream);
        } catch (SAXException e) {
            throw new InputXmlException(e);
        }

        if (handler.errors.size() > 0)
            throw new InputXmlException(handler.errors);

        return document;
    }

    private static Schema createSchema() {
        // xsd ファイルをリソースから取り出す
        URL schemaUrl = Resources.getResourceUrl(Resources.REPORT_SCHEMA_DEFINITION);

        try {
            // xsd ファイルを Schema として読む
            return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(schemaUrl);
        } catch (SAXException e) {
            // スキーマファイル自体にエラーがあり、回復不可能
            throw new AssertionError("スキーマファイルにエラー", e);
        }
    }

    private static DocumentBuilder createDocumentBuilder() {
        Schema schema = createSchema();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false); // DTD 検証を無効化
        factory.setSchema(schema); // XML Schema による検証を使用

        try {
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError("DocumentBuilderFactory に対して不適切な設定", e);
        }
    }

    private static class InputHandler extends DefaultHandler {
        final List<SAXException> errors = new ArrayList<>();

        @Override
        public void error(SAXParseException e) {
            errors.add(e);
        }
    }
}
