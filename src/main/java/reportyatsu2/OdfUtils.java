package reportyatsu2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public final class OdfUtils {
    private OdfUtils() {}

    public static final String NS_OFFICE = "urn:oasis:names:tc:opendocument:xmlns:office:1.0";
    public static final String NS_DRAW = "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0";
    public static final String NS_MANIFEST = "urn:oasis:names:tc:opendocument:xmlns:manifest:1.0";
    public static final String NS_TABLE = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";
    public static final String NS_TEXT = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
    public static final String NS_STYLE = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
    public static final String NS_FO = "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0";
    public static final String NS_XLINK = "http://www.w3.org/1999/xlink";
    public static final String NS_XMLNS = "http://www.w3.org/2000/xmlns/";

    public static final String STYLE_STANDARD = "Standard";
    public static final String STYLE_TITLE = "Title";
    public static final String STYLE_PARAGRAPH = "Paragraph";
    public static final String STYLE_HEADING_PREFIX = "Heading";
    public static final String STYLE_IMAGE_BLOCK = "ImageBlock";
    public static final String STYLE_BLOCK_CAPTION = "BlockCaption";
    public static final String STYLE_TABLE_HEADING = "Table.Heading";
    public static final String STYLE_CODE_BLOCK_CODE = "CodeBlock.Code";
    public static final String STYLE_CODE_BLOCK_LINE_NUMBER = "CodeBlock.LineNumber";
    public static final String STYLE_CODE = "Code";
    public static final String STYLE_UNORDERED_LIST = "UnorderedList";
    public static final String STYLE_ORDERED_LIST = "OrderedList";
    public static final String STYLE_BIBLIOGRAPHY_LIST = "BibliographyList";
    public static final String STYLE_STANDARD_TABLE = "StandardTable";
    public static final String STYLE_STANDARD_TABLE_CELL = "StandardTable.Cell";
    public static final String STYLE_CODE_BLOCK_TABLE = "CodeBlock.Table";
    public static final String STYLE_CODE_BLOCK_LINE_NUMBER_COLUMN = "CodeBlock.LineNumberColumn";
    public static final String STYLE_CODE_BLOCK_CODE_COLUMN = "CodeBlock.CodeColumn";
    public static final String STYLE_CODE_BLOCK_LINE_NUMBER_CELL = "CodeBlock.LineNumberCell";
    public static final String STYLE_CODE_BLOCK_CODE_CELL = "CodeBlock.CodeCell";
    public static final String STYLE_CODE_BLOCK_LINE_NUMBER_CELL_FIRST = "CodeBlock.LineNumberCell.First";
    public static final String STYLE_CODE_BLOCK_CODE_CELL_FIRST = "CodeBlock.CodeCell.First";
    public static final String STYLE_CODE_BLOCK_LINE_NUMBER_CELL_LAST = "CodeBlock.LineNumberCell.Last";
    public static final String STYLE_CODE_BLOCK_CODE_CELL_LAST = "CodeBlock.CodeCell.Last";
    public static final String STYLE_CODE_BLOCK_LINE_NUMBER_CELL_ONE_LINE = "CodeBlock.LineNumberCell.OneLine";
    public static final String STYLE_CODE_BLOCK_CODE_CELL_ONE_LINE = "CodeBlock.CodeCell.OneLine";

    public static final String OPEN_DOCUMENT_TEXT_MIME_TYPE = "application/vnd.oasis.opendocument.text";

    public static final String CONTENT_XML_PATH = "content.xml";
    public static final String STYLES_XML_PATH = "styles.xml";
    public static final String MANIFEST_XML_PATH = "META-INF/manifest.xml";

    public static List<Node> createNodesForText(Document document, String text) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case ' ':
                    // スペースは <text:s /> に変換
                    nodes.add(createSpaceElement(document));
                    break;
                case '\n':
                    // 改行は <text:line-break /> に変換
                    nodes.add(createLineBreakElement(document));
                case '\r':
                    // CR は無視
                    break;
                default:
                    nodes.add(document.createTextNode(String.valueOf(c)));
            }
        }
        return nodes;
    }

    public static Element createSpaceElement(Document document) {
        return document.createElementNS(NS_TEXT, "s");
    }

    public static Element createLineBreakElement(Document document) {
        return document.createElementNS(NS_TEXT, "line-break");
    }

    public static Element createParagraphElement(Document document, String styleName) {
        Element paragraph = document.createElementNS(NS_TEXT, "p");
        paragraph.setAttributeNS(NS_TEXT, "style-name", styleName);
        return paragraph;
    }

    public static Element createBookmarkStart(Document document, String name) {
        Element bookmarkStart = document.createElementNS(NS_TEXT, "bookmark-start");
        bookmarkStart.setAttributeNS(NS_TEXT, "name", name);
        return bookmarkStart;
    }

    public static Element createBookmarkEnd(Document document, String name) {
        Element bookmarkEnd = document.createElementNS(NS_TEXT, "bookmark-end");
        bookmarkEnd.setAttributeNS(NS_TEXT, "name", name);
        return bookmarkEnd;
    }

    public static Element createChapterBookmarkRef(Document document, String name, String text) {
        Element bookmarkRef = document.createElementNS(NS_TEXT, "bookmark-ref");
        bookmarkRef.setAttributeNS(NS_TEXT, "reference-format", "chapter");
        bookmarkRef.setAttributeNS(NS_TEXT, "ref-name", name);

        for (Node node : createNodesForText(document, text))
            bookmarkRef.appendChild(node);

        return bookmarkRef;
    }
}
