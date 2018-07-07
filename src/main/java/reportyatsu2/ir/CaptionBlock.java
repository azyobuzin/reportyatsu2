package reportyatsu2.ir;

public abstract class CaptionBlock extends Block implements Referable {
    private final String id;
    private final Integer sequenceNumber;
    private final InlineElementList caption;

    protected CaptionBlock(String id, Integer sequenceNumber, InlineElementList caption) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.caption = caption;
    }

    @Override
    public String getId() { return id; }

    public Integer getSequenceNumber() { return sequenceNumber; }

    public InlineElementList getCaption() { return caption; }
}
