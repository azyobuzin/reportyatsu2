package reportyatsu2.ir;

import java.util.ArrayDeque;

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
