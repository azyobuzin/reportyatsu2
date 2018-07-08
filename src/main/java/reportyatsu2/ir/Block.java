package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.List;

public abstract class Block {
    public abstract List<Node> createNodes(Document document, InputToIrTransformResult irResult);
}
