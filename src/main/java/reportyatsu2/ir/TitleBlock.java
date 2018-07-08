package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.Collections;
import java.util.List;

import static reportyatsu2.OdfUtils.*;

public class TitleBlock extends Block {
    private final String title;

    public TitleBlock(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        Element paragraph = createParagraphElement(document, STYLE_TITLE);
        for (Node node : createNodesForText(document, getTitle()))
            paragraph.appendChild(node);
        return Collections.singletonList(paragraph);
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
