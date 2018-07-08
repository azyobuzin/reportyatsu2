package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import static reportyatsu2.OdfUtils.*;

import java.util.Collections;
import java.util.List;

public class ParagraphBlock extends Block {
    private final InlineElementList inlineElements;

    public ParagraphBlock(InlineElementList inlineElements) {
        this.inlineElements = inlineElements;
    }

    public InlineElementList getInlineElements() { return inlineElements; }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        Element paragraph = createParagraphElement(document, STYLE_PARAGRAPH);
        for (Node node : getInlineElements().createNodes(document, irResult))
            paragraph.appendChild(node);
        return Collections.singletonList(paragraph);
    }

    @Override
    public String toString() { return getInlineElements().toString(); }
}
