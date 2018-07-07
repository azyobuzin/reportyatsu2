package reportyatsu2.ir;

public class TextElement extends InlineElement {
    private final String text;

    public TextElement(String text) {
        this.text = text;
    }

    public String getText() { return text; }

    @Override
    public String toString() {
        return getText();
    }
}
