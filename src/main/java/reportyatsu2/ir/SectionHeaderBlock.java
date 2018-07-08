package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;

import static reportyatsu2.OdfUtils.*;

public class SectionHeaderBlock extends Block implements Referable {
    private final String id;
    private final String title;
    private final SectionHeaderBlock parentSection;
    private final int sequenceNumber;

    public SectionHeaderBlock(String id, String title, SectionHeaderBlock parentSection, int sequenceNumber) {
        this.id = id;
        this.title = title;
        this.parentSection = parentSection;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String getId() { return id; }

    public String getTitle() { return title; }

    public SectionHeaderBlock getParentSection() { return parentSection; }

    public int getSequenceNumber() { return sequenceNumber; }

    public int getOutlineLevel() {
        SectionHeaderBlock parentSection = getParentSection();
        // 自分が最上位（章）なら1、そうでないなら親+1で深さが求まる
        return parentSection == null ? 1 : parentSection.getOutlineLevel() + 1;
    }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        int outlineLevel = getOutlineLevel();
        Element header = document.createElementNS(NS_TEXT, "h");
        // Heading1, Heading2, ... という名前付け
        header.setAttributeNS(NS_TEXT, "style-name", STYLE_HEADING_PREFIX + outlineLevel);
        header.setAttributeNS(NS_TEXT, "outline-level", Integer.toString(outlineLevel));

        // ID が指定されているなら bookmark を設定
        String id = getId();
        if (id != null) header.appendChild(createBookmarkStart(document, id));

        for (Node node : createNodesForText(document, getTitle()))
            header.appendChild(node);

        if (id != null) header.appendChild(createBookmarkEnd(document, id));

        return Collections.singletonList(header);
    }

    private static final String[] REFERENCE_SUFFIXES = {"章", "節", "項"};

    @Override
    public List<Node> createReferenceNodes(Document document) {
        // 「1章」、「1.1節」、「1.1.1項」のように表示
        String text = getDisplaySequenceNumber() + REFERENCE_SUFFIXES[getOutlineLevel() - 1];
        Element bookmarkRef = createChapterBookmarkRef(document, getId(), text);
        return Collections.singletonList(bookmarkRef);
    }

    private String getDisplaySequenceNumber() {
        // 連番を子から親の順で求めて、それを逆順に記録
        ArrayDeque<String> seqNumList = new ArrayDeque<>();
        for (SectionHeaderBlock section = this; section != null; section = section.getParentSection())
            seqNumList.addFirst(Integer.toString(section.getSequenceNumber()));
        return String.join(".", seqNumList);
    }

    @Override
    public String toString() {
        return String.format("%s. %s", getDisplaySequenceNumber(), getTitle());
    }
}
