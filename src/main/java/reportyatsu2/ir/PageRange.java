package reportyatsu2.ir;

public class PageRange {
    private int start;
    private int end;

    public PageRange(int start, int end) {
        if (start < 1 || start > end)
            throw new IllegalArgumentException("ページ番号は正で、 end は start 以上である必要があります。");
        this.start = start;
        this.end = end;
    }

    public PageRange(int page) { this(page, page); }

    public int getStart() { return start; }

    public int getEnd() { return end; }
}
