package reportyatsu2.ir;

public class TitleBlock extends Block {
    private final String title;

    public TitleBlock(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }

    @Override
    public String toString() {
        return getTitle();
    }
}
