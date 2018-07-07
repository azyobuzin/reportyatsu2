package reportyatsu2.ir;

import java.util.List;

public class Literature implements Referable {
    private final String id;
    private final String title;
    private final String author;
    private final String publisher;
    private final String pubDate;
    private final List<PageRange> pages;
    private final String href;
    private final String browseDate;

    public Literature(String id, String title, String author, String publisher, String pubDate, List<PageRange> pages, String href, String browseDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.pubDate = pubDate;
        this.pages = pages;
        this.href = href;
        this.browseDate = browseDate;
    }

    @Override
    public String getId() { return id; }

    public String getTitle() { return title; }

    public String getAuthor() { return author; }

    public String getPublisher() { return publisher; }

    public String getPubDate() { return pubDate; }

    public List<PageRange> getPages() { return pages; }

    public String getHref() { return href; }

    public String getBrowseDate() { return browseDate; }
}
