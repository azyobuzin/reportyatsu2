package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.List;

import static reportyatsu2.OdfUtils.createNodesForText;

public class TextElement extends InlineElement {
    private final String text;

    public TextElement(String text) {
        this.text = text;
    }

    public String getText() { return text; }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        return createNodesForText(document, getText());
    }

    @Override
    public String toString() {
        return getText();
    }
}
