package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InlineElementList extends ArrayList<InlineElement> {
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        return this.stream()
            .flatMap(e -> e.createNodes(document, irResult).stream())
            .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (InlineElement e : this)
            sb.append(e);
        return sb.toString();
    }
}
