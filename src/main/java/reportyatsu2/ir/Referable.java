package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.List;

public interface Referable {
    String getId();

    List<Node> createReferenceNodes(Document document);
}
