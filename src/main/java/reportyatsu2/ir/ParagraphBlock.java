package reportyatsu2.ir;

public class ParagraphBlock extends Block {
    private final InlineElementList inlineElements;

    public ParagraphBlock(InlineElementList inlineElements) {
        this.inlineElements = inlineElements;
    }

    public InlineElementList getInlineElements() { return inlineElements; }

    @Override
    public String toString() { return getInlineElements().toString(); }
}
