package reportyatsu2.ir;

public class ListItem {
    private final InlineElementList inlineElements;
    private final ListOfItems childList;

    public ListItem(InlineElementList inlineElements, ListOfItems childList) {
        this.inlineElements = inlineElements;
        this.childList = childList;
    }

    public InlineElementList getInlineElements() { return inlineElements; }

    public ListOfItems getChildList() { return childList; }
}
