package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import reportyatsu2.InputToIrTransformResult;

import java.util.ArrayList;

import static reportyatsu2.OdfUtils.*;

public class ListOfItems extends ArrayList<ListItem> {
    public Element createElement(Document document, InputToIrTransformResult irResult) {
        org.w3c.dom.Element list = document.createElementNS(NS_TEXT, "text:list");
        for (ListItem item : this)
            list.appendChild(item.createElement(document, irResult));
        return list;
    }
}
