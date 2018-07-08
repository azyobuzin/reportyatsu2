package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.List;

public class ReferenceElement extends InlineElement {
    private final String targetId;

    public ReferenceElement(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetId() { return targetId; }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        return irResult.getIdMap().get(getTargetId()).createReferenceNodes(document);
    }

    @Override
    public String toString() {
        return String.format("[ref:%s]", getTargetId());
    }
}
