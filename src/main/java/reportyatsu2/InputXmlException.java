package reportyatsu2;

import org.xml.sax.SAXException;

import java.util.List;
import java.util.stream.Collectors;

public class InputXmlException extends Exception {
    public InputXmlException(SAXException cause) {
        super(cause.getMessage(), cause);
    }

    public InputXmlException(List<SAXException> errors) {
        super(errors.stream()
            .map(Exception::getMessage)
            .collect(Collectors.joining(System.lineSeparator())));
    }
}
