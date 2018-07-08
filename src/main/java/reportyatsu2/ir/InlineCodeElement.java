package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import static reportyatsu2.OdfUtils.*;

import java.util.Collections;
import java.util.List;

public class InlineCodeElement extends InlineElement {
    private final String language;
    private final String code;

    public InlineCodeElement(String language, String code) {
        this.code = code;
        this.language = language;
    }

    public String getLanguage() { return language; }

    public String getCode() { return code; }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        Element span = document.createElementNS(NS_TEXT, "span");
        span.setAttributeNS(NS_TEXT, "style-name", STYLE_CODE);

        for (Node node : createNodesForText(document, getCode()))
            span.appendChild(node);

        return Collections.singletonList(span);
    }

    @Override
    public String toString() {
        String lang = getLanguage();
        if (lang == null) lang = "code";
        return String.format("[%s:%s]", lang, getCode());
    }
}
