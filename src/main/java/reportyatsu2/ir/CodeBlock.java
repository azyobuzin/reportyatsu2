package reportyatsu2.ir;

public class CodeBlock extends CaptionBlock {
    private final String language;
    private final String code;

    public CodeBlock(String id, Integer sequenceNumber, InlineElementList caption, String language, String code) {
        super(id, sequenceNumber, caption);
        this.language = language;
        this.code = code;
    }

    public String getLanguage() { return language; }

    public String getCode() { return code; }

    @Override
    public String toString() {
        return new StringBuilder("```")
                .append(getLanguage()).append(System.lineSeparator())
                .append(getCode()).append(System.lineSeparator())
                .append("```").toString();
    }
}
