package reportyatsu2.ir;

public class InlineCodeElement extends InlineElement {
    private final String language;
    private final String code;

    public InlineCodeElement(String language, String code) {
        this.code = code;
        this.language = language;
    }

    public String getLanguage() { return language; }

    public String getCode() { return code; }

    @Override
    public String toString() {
        String lang = getLanguage();
        if (lang == null) lang = "code";
        return String.format("[%s:%s]", lang, getCode());
    }
}
