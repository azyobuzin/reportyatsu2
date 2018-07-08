package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import static reportyatsu2.OdfUtils.*;

public class ListItem {
    private final InlineElementList inlineElements;
    private final ListOfItems childList;

    public ListItem(InlineElementList inlineElements, ListOfItems childList) {
        this.inlineElements = inlineElements;
        this.childList = childList;
    }

    public InlineElementList getInlineElements() { return inlineElements; }

    public ListOfItems getChildList() { return childList; }

    public Element createElement(Document document, InputToIrTransformResult irResult) {
        Element paragraph = createParagraphElement(document, STYLE_STANDARD);
        for (Node node : inlineElements.createNodes(document, irResult))
            paragraph.appendChild(node);

        Element listItem = document.createElementNS(NS_TEXT, "list-item");
        listItem.appendChild(paragraph);

        ListOfItems childList = getChildList();
        if (childList != null)
            listItem.appendChild(childList.createElement(document, irResult));

        return listItem;
    }
}
