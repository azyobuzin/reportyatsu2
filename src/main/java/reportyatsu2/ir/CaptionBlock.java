package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import static reportyatsu2.OdfUtils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CaptionBlock extends Block implements Referable {
    private final String id;
    private final int sequenceNumber;
    private final InlineElementList caption;

    protected CaptionBlock(String id, int sequenceNumber, InlineElementList caption) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.caption = caption;
    }

    @Override
    public String getId() { return id; }

    public int getSequenceNumber() { return sequenceNumber; }

    public InlineElementList getCaption() { return caption; }

    @Override
    public List<Node> createReferenceNodes(Document document) {
        Element sequenceRef = document.createElementNS(NS_TEXT, "sequence-ref");
        sequenceRef.setAttributeNS(NS_TEXT, "reference-format", "category-and-value");
        sequenceRef.setAttributeNS(NS_TEXT, "ref-name", getId());

        // 「図1」のような形式
        String text = getSequenceDisplayName() + getSequenceNumber();
        for (Node node : createNodesForText(document, text))
            sequenceRef.appendChild(node);

        return Collections.singletonList(sequenceRef);
    }

    protected abstract String getSequenceName();

    protected abstract String getSequenceDisplayName();

    protected List<Node> createCaptionContentNodes(Document document, InputToIrTransformResult irResult) {
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(createNodesForText(document, getSequenceDisplayName()));

        Element sequence = document.createElementNS(NS_TEXT, "sequence");
        nodes.add(sequence);

        String id = getId();
        if (id != null) sequence.setAttributeNS(NS_TEXT, "ref-name", id);
        sequence.setAttributeNS(NS_TEXT, "name", getSequenceName());
        sequence.setAttributeNS(NS_STYLE, "num-format", "1");

        for (Node node : createNodesForText(document, Integer.toString(getSequenceNumber())))
            sequence.appendChild(node);

        InlineElementList caption = getCaption();
        if (caption != null) {
            nodes.add(document.createTextNode(":"));
            nodes.add(createSpaceElement(document));
            nodes.addAll(caption.createNodes(document, irResult));
        }

        return nodes;
    }

    protected Element createCaptionParagraph(Document document, InputToIrTransformResult irResult) {
        Element paragraph = createParagraphElement(document, STYLE_BLOCK_CAPTION);
        for (Node node : createCaptionContentNodes(document, irResult))
            paragraph.appendChild(node);
        return paragraph;
    }
}
