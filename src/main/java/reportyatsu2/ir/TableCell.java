package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import static reportyatsu2.OdfUtils.*;

public class TableCell {
    private boolean isHeader;
    private final InlineElementList inlineElements;

    public TableCell(boolean isHeader, InlineElementList inlineElements) {
        this.isHeader = isHeader;
        this.inlineElements = inlineElements;
    }

    public boolean isHeader() { return isHeader; }

    public InlineElementList getInlineElements() { return inlineElements; }

    public Element createElement(Document document, InputToIrTransformResult irResult) {
        String paragraphStyle = isHeader() ? STYLE_TABLE_HEADING : STYLE_STANDARD;
        Element paragraph = createParagraphElement(document, paragraphStyle);
        for (Node node : getInlineElements().createNodes(document, irResult))
            paragraph.appendChild(node);

        Element cell = document.createElementNS(NS_TABLE, "table:table-cell");
        cell.setAttributeNS(NS_TABLE, "table:style-name", STYLE_STANDARD_TABLE_CELL);
        cell.appendChild(paragraph);
        return cell;
    }

    @Override
    public String toString() {
        return isHeader() ? "**" + getInlineElements() + "**"
            : getInlineElements().toString();
    }
}
