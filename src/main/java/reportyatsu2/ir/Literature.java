package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Collections;
import java.util.List;

import static reportyatsu2.OdfUtils.*;

public class Literature implements Referable {
    private final String id;
    private final int sequenceNumber;
    private final String title;
    private final String author;
    private final String publisher;
    private final String pubDate;
    private final List<PageRange> pages;
    private final String href;
    private final String browseDate;

    public Literature(String id, int sequenceNumber, String title, String author, String publisher, String pubDate, List<PageRange> pages, String href, String browseDate) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
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

    public int getSequenceNumber() { return sequenceNumber; }

    public String getTitle() { return title; }

    public String getAuthor() { return author; }

    public String getPublisher() { return publisher; }

    public String getPubDate() { return pubDate; }

    public List<PageRange> getPages() { return pages; }

    public String getHref() { return href; }

    public String getBrowseDate() { return browseDate; }

    private static final String DELIMITER = "、";

    public String toStringWithoutSequenceNumber() {
        StringBuilder sb = new StringBuilder();

        String author = getAuthor();
        if (author != null)
            sb.append(author).append('：');

        sb.append('“').append(getTitle()).append('”');

        String publisher = getPublisher();
        if (publisher != null)
            sb.append(DELIMITER).append(publisher);

        List<PageRange> pages = getPages();
        if (pages != null) {
            for (PageRange range : pages) {
                sb.append(DELIMITER);
                if (range.getStart() == range.getEnd()) {
                    sb.append("p.").append(range.getStart());
                } else {
                    sb.append("pp.").append(range.getStart())
                        .append('-').append(range.getEnd());
                }
            }
        }

        String pubDate = getPubDate();
        if (pubDate != null) {
            sb.append(DELIMITER);
            appendDate(pubDate, sb);
        }

        String browseDate = getBrowseDate();
        if (browseDate != null) {
            sb.append(DELIMITER);
            appendDate(browseDate, sb);
        }

        String href = getHref();
        if (href != null) {
            sb.append(System.lineSeparator())
                .append(href);
        }

        return sb.toString();
    }

    public Element createElement(Document document) {
        Element paragraph = createParagraphElement(document, STYLE_LITERATURE);

        // ID があるなら参照できるように bookmark を設定する
        String id = getId();
        if (id != null) paragraph.appendChild(createBookmarkStart(document, id));

        // 内容を追加
        for (Node node : createNodesForText(document, toStringWithoutSequenceNumber()))
            paragraph.appendChild(node);

        // bookmark 終わり
        if (id != null) paragraph.appendChild(createBookmarkEnd(document, id));

        Element listItem = document.createElementNS(NS_TEXT, "text:list-item");
        listItem.appendChild(paragraph);
        return listItem;
    }

    @Override
    public List<Node> createReferenceNodes(Document document) {
        return Collections.singletonList(createChapterBookmarkRef(
            document, getId(), String.format("[%d]", getSequenceNumber())));
    }

    @Override
    public String toString() {
        return String.format("[%d] %s", getSequenceNumber(), toStringWithoutSequenceNumber());
    }

    private static void appendDate(String date, StringBuilder sb) {
        String[] split = date.split("-", 3);
        if (split.length <= 0) return;
        sb.append(split[0]).append('年');
        if (split.length <= 1) return;
        sb.append(split[1]).append('月');
        if (split.length <= 2) return;
        sb.append(split[2]).append('日');
    }
}
