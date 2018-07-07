package reportyatsu2.ir;

public class TableCell {
    private boolean isHeader;
    private final InlineElementList inlineElements;

    public TableCell(boolean isHeader, InlineElementList inlineElements) {
        this.isHeader = isHeader;
        this.inlineElements = inlineElements;
    }

    public boolean isHeader() { return isHeader; }

    public InlineElementList getInlineElements() { return inlineElements; }

    @Override
    public String toString() {
        return isHeader() ? "**" + getInlineElements() + "**"
                : getInlineElements().toString();
    }
}
