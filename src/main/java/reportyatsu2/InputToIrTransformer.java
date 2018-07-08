package reportyatsu2;

import org.w3c.dom.*;
import reportyatsu2.ir.*;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class InputToIrTransformer implements InputToIrTransformResult {
    private static final String ROOT_ELEMENT_NAME = "report";
    private static final String SECTION_ELEMENT_NAME = "section";
    private static final String PARAGRAPH_ELEMENT_NAME = "p";
    private static final String UNORDERED_LIST_ELEMENT_NAME = "ul";
    private static final String ORDERED_LIST_ELEMENT_NAME = "ol";
    private static final String LIST_ITEM_ELEMENT_NAME = "li";
    private static final String FIGURE_ELEMENT_NAME = "figure";
    private static final String TABLE_ELEMENT_NAME = "table";
    private static final String CODE_BLOCK_ELEMENT_NAME = "codeBlock";
    private static final String BIBLIOGRAPHY_ELEMENT_NAME = "bibliography";
    private static final String LITERATURE_ELEMENT_NAME = "literature";
    private static final String REFERENCE_ELEMENT_NAME = "ref";
    private static final String CODE_ELEMENT_NAME = "code";
    private static final String CAPTION_ELEMENT_NAME = "caption";
    private static final String ID_ATTRIBUTE_NAME = "id";
    private static final String CODE_LANGUAGE_ATTRIBUTE_NAME = "lang";

    private final boolean debugMode;
    private final Path workingDirectory;
    private final List<Block> blocks = new ArrayList<>();
    private final Map<String, Referable> idMap = new HashMap<>();
    private int figureSequenceNumber;
    private int tableSequenceNumber;
    private int listSequenceNumber;
    private int literatureSequenceNumber;
    private int maxSectionDepth;
    private int maxUnorderedListDepth;
    private int maxOrderedListDepth;

    public InputToIrTransformer(boolean debugMode, Path workingDirectory) {
        this.debugMode = debugMode;
        this.workingDirectory = workingDirectory;
    }

    public void inputDocument(Document document) throws InputToIrTransformException {
        Element root = document.getDocumentElement();
        transformRoot(root);
    }

    @Override
    public List<Block> getBlocks() { return Collections.unmodifiableList(blocks); }

    @Override
    public Map<String, Referable> getIdMap() { return Collections.unmodifiableMap(idMap); }

    @Override
    public int getMaxSectionDepth() { return maxSectionDepth; }

    @Override
    public int getMaxUnorderedListDepth() { return maxUnorderedListDepth; }

    @Override
    public int getMaxOrderedListDepth() { return maxOrderedListDepth; }

    private void log(String format, Object... args) {
        // デバッグモードのときだけログを出力
        if (debugMode) System.err.println(String.format(format, args));
    }

    private void addBlock(Block block) {
        blocks.add(block);
        log("%s", block);
    }

    private void registerReferable(Referable element) {
        String id = element.getId();
        if (id != null) {
            idMap.put(id, element);
            log("ID: %s", id);
        }
    }

    private void transformRoot(Element reportElement) throws InputToIrTransformException {
        assert ROOT_ELEMENT_NAME.equals(reportElement.getTagName());

        // タイトルが設定されているなら、タイトルを出力
        String title = getAttributeOrNull(reportElement, "title");
        if (title != null) addBlock(new TitleBlock(title));

        // 子 section を処理
        int sequenceNumber = 0;
        Iterator<Element> sections = childElements(reportElement).iterator();
        while (sections.hasNext())
            transformSection(sections.next(), null, ++sequenceNumber);

        // 見出しを 3 までしか用意していないので、それより深くなるならばエラー
        final int SUPPORTED_OUTLINE_LEVEL = 3;
        if (getMaxSectionDepth() > SUPPORTED_OUTLINE_LEVEL)
            throw new InputToIrTransformException("section が深すぎます。");
    }

    private void transformSection(Element sectionElement, SectionHeaderBlock parentSection, int sequenceNumber) throws InputToIrTransformException {
        assert SECTION_ELEMENT_NAME.equals(sectionElement.getTagName());

        // 見出しを作成
        SectionHeaderBlock header = new SectionHeaderBlock(
            getAttributeOrNull(sectionElement, ID_ATTRIBUTE_NAME),
            getAttributeOrNull(sectionElement, "title"),
            parentSection,
            sequenceNumber);
        addBlock(header);

        // セクションの最大深さを更新
        int outlineLevel = header.getOutlineLevel();
        maxSectionDepth = Math.max(maxSectionDepth, outlineLevel);

        // 子要素を処理
        int childSectionNumber = 0;
        Iterator<Element> children = childElements(sectionElement).iterator();
        while (children.hasNext()) {
            Element child = children.next();
            if (SECTION_ELEMENT_NAME.equals(child.getTagName())) {
                // 子 section は連番をつける
                transformSection(child, header, ++childSectionNumber);
            } else {
                // section でないものはブロックとして扱う
                transformBlock(child);
            }
        }
    }

    private void transformBlock(Element blockElement) throws InputToIrTransformException {
        String tagName = blockElement.getTagName();
        switch (tagName) {
            case PARAGRAPH_ELEMENT_NAME:
                transformParagraphBlock(blockElement);
                break;
            case UNORDERED_LIST_ELEMENT_NAME:
                transformUnorderedListBlock(blockElement);
                break;
            case ORDERED_LIST_ELEMENT_NAME:
                transformOrderedListBlock(blockElement);
                break;
            case FIGURE_ELEMENT_NAME:
                transformFigureBlock(blockElement);
                break;
            case TABLE_ELEMENT_NAME:
                transformTableBlock(blockElement);
                break;
            case CODE_BLOCK_ELEMENT_NAME:
                transformCodeBlock(blockElement);
                break;
            case BIBLIOGRAPHY_ELEMENT_NAME:
                transformBibliographyBlock(blockElement);
                break;
            default:
                throw new InputToIrTransformException(String.format("認識できない要素 <%s>", tagName));
        }
    }

    private InlineElementList transformInlineElements(Node container) throws InputToIrTransformException {
        InlineElementList elements = new InlineElementList();
        NodeList nodes = container.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            switch (node.getNodeType()) {
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE: {
                    boolean isFirstElement = elements.isEmpty();
                    InlineElement prevElement = isFirstElement ? null : elements.get(elements.size() - 1);
                    boolean isPrevElementText = prevElement instanceof TextElement;

                    // ノードの中身を取得
                    String text = ((CharacterData) node).getData();

                    // 前の要素もテキストなら、結合して空白処理をする
                    if (isPrevElementText) text = ((TextElement) prevElement).getText() + text;

                    // 空白をうまいことする。最初の要素ならば、最初の空白もすべて削除する
                    text = normalizeText(text, isFirstElement);

                    // 結合したなら上書き、そうでないなら追加
                    TextElement element = new TextElement(text);
                    if (isPrevElementText) elements.set(elements.size() - 1, element);
                    else elements.add(element);

                    break;
                }
                case Node.ELEMENT_NODE: {
                    Element element = (Element) node;
                    String tagName = element.getTagName();
                    switch (tagName) {
                        case REFERENCE_ELEMENT_NAME:
                            elements.add(transformReference(element));
                            break;
                        case CODE_ELEMENT_NAME:
                            elements.add(transformInlineCode(element));
                            break;
                        case UNORDERED_LIST_ELEMENT_NAME:
                        case ORDERED_LIST_ELEMENT_NAME:
                            // リストはここでは無視
                            break;
                        default:
                            throw new InputToIrTransformException(String.format("認識できない要素 <%s>", tagName));
                    }
                    break;
                }
            }
        }
        return elements;
    }

    private ReferenceElement transformReference(Element refElement) {
        assert REFERENCE_ELEMENT_NAME.equals(refElement.getTagName());
        return new ReferenceElement(refElement.getAttribute("target"));
    }

    private InlineCodeElement transformInlineCode(Element codeElement) {
        assert CODE_ELEMENT_NAME.equals(codeElement.getTagName());
        return new InlineCodeElement(
            getAttributeOrNull(codeElement, CODE_LANGUAGE_ATTRIBUTE_NAME),
            getCodeContent(codeElement));
    }

    private static final Pattern LEADING_EMPTY_LINES_PATTERN = Pattern.compile("^([\\s&&[^\\n]]*\\n)+");

    private String getCodeContent(Element codeElement) {
        assert CODE_ELEMENT_NAME.equals(codeElement.getTagName());

        String content = codeElement.getTextContent();

        // 最初の空行を削除する
        content = LEADING_EMPTY_LINES_PATTERN.matcher(content).replaceAll("");

        // 後ろから順に読んでいき、空白でない文字があったら、そこまでを返す
        for (int i = content.length() - 1; i >= 0; i--) {
            if (!Character.isWhitespace(content.charAt(i)))
                return content.substring(0, i + 1);
        }

        return ""; // すべての文字が空白だった……
    }

    private void transformParagraphBlock(Element pElement) throws InputToIrTransformException {
        assert PARAGRAPH_ELEMENT_NAME.equals(pElement.getTagName());
        InlineElementList inlineElements = transformInlineElements(pElement);
        addBlock(new ParagraphBlock(inlineElements));
    }

    private void transformUnorderedListBlock(Element ulElement) throws InputToIrTransformException {
        assert UNORDERED_LIST_ELEMENT_NAME.equals(ulElement.getTagName());

        ListOfItems list = transformList(ulElement);
        ListBlock block = new ListBlock(false, list);
        addBlock(block);

        // 最大深さ更新
        maxUnorderedListDepth = Math.max(maxUnorderedListDepth, block.getDepth());
    }

    private void transformOrderedListBlock(Element olElement) throws InputToIrTransformException {
        assert ORDERED_LIST_ELEMENT_NAME.equals(olElement.getTagName());

        ListOfItems list = transformList(olElement);
        ListBlock block = new ListBlock(true, list);
        addBlock(block);

        // 最大深さ更新
        maxOrderedListDepth = Math.max(maxOrderedListDepth, block.getDepth());
    }

    private ListOfItems transformList(Element listElement) throws InputToIrTransformException {
        ListOfItems list = new ListOfItems();
        Iterator<Element> elements = childElements(listElement).iterator();
        while (elements.hasNext())
            list.add(transformListItem(elements.next()));
        return list;
    }

    private ListItem transformListItem(Element liElement) throws InputToIrTransformException {
        assert LIST_ITEM_ELEMENT_NAME.equals(liElement.getTagName());

        InlineElementList inlineElements = transformInlineElements(liElement);

        // 子リストがあるなら読み込む
        Optional<Element> childListElement = childElements(liElement)
            .filter(element -> {
                String tagName = element.getTagName();
                return UNORDERED_LIST_ELEMENT_NAME.equals(tagName)
                    || ORDERED_LIST_ELEMENT_NAME.equals(tagName);
            })
            .findFirst();

        // Optional.map が検査例外のせいで使えない～～～
        ListOfItems childList = childListElement.isPresent()
            ? transformList(childListElement.get()) : null;

        return new ListItem(inlineElements, childList);
    }

    private InlineElementList transformCaption(Element captionContainer) throws InputToIrTransformException {
        Element element = childElement(captionContainer, CAPTION_ELEMENT_NAME);
        return element == null ? null : transformInlineElements(element);
    }

    private void transformFigureBlock(Element figureElement) throws InputToIrTransformException {
        assert FIGURE_ELEMENT_NAME.equals(figureElement.getTagName());

        int sequenceNumber = ++figureSequenceNumber;
        String zoomString = getAttributeOrNull(figureElement, "zoom");
        double zoom = zoomString != null ? Double.parseDouble(zoomString) : 1.0;

        FigureBlock block = new FigureBlock(
            getAttributeOrNull(figureElement, ID_ATTRIBUTE_NAME),
            sequenceNumber,
            transformCaption(figureElement),
            workingDirectory,
            getAttributeOrNull(figureElement, "src"),
            zoom);

        registerReferable(block);
        addBlock(block);
    }

    private void transformTableBlock(Element tableElement) throws InputToIrTransformException {
        assert TABLE_ELEMENT_NAME.equals(tableElement.getTagName());

        int sequenceNumber = ++tableSequenceNumber;

        List<TableRow> rows = new ArrayList<>();
        Iterator<Element> rowElements = childElements(tableElement, "tr").iterator();
        while (rowElements.hasNext()) {
            List<TableCell> cells = new ArrayList<>();
            Iterator<Element> cellElements = childElements(rowElements.next()).iterator();
            while (cellElements.hasNext()) {
                Element cellElement = cellElements.next();
                cells.add(new TableCell(
                    "th".equals(cellElement.getTagName()),
                    transformInlineElements(cellElement)));
            }
            rows.add(new TableRow(cells));
        }

        TableBlock block = new TableBlock(
            getAttributeOrNull(tableElement, ID_ATTRIBUTE_NAME),
            sequenceNumber,
            transformCaption(tableElement),
            rows);

        registerReferable(block);
        addBlock(block);
    }

    private void transformCodeBlock(Element codeBlockElement) throws InputToIrTransformException {
        assert CODE_BLOCK_ELEMENT_NAME.equals(codeBlockElement.getTagName());

        int sequenceNumber = ++listSequenceNumber;
        Element codeElement = childElement(codeBlockElement, CODE_ELEMENT_NAME);

        CodeBlock block = new CodeBlock(
            getAttributeOrNull(codeBlockElement, ID_ATTRIBUTE_NAME),
            sequenceNumber,
            transformCaption(codeBlockElement),
            getAttributeOrNull(codeElement, CODE_LANGUAGE_ATTRIBUTE_NAME),
            getCodeContent(codeElement));

        registerReferable(block);
        addBlock(block);
    }

    private void transformBibliographyBlock(Element bibliographyElement) throws InputToIrTransformException {
        assert BIBLIOGRAPHY_ELEMENT_NAME.equals(bibliographyElement.getTagName());
        List<Literature> literatureList = new ArrayList<>();
        Iterator<Element> childElements = childElements(bibliographyElement).iterator();
        while (childElements.hasNext())
            literatureList.add(transformLiterature(childElements.next()));
        addBlock(new BibliographyBlock(literatureList));
    }

    private Literature transformLiterature(Element literatureElement) throws InputToIrTransformException {
        assert LITERATURE_ELEMENT_NAME.equals(literatureElement.getTagName());

        int sequenceNumber = ++literatureSequenceNumber;
        String pages = getAttributeOrNull(literatureElement, "pages");

        Literature literature = new Literature(
            getAttributeOrNull(literatureElement, ID_ATTRIBUTE_NAME),
            sequenceNumber,
            getAttributeOrNull(literatureElement, "title"),
            getAttributeOrNull(literatureElement, "author"),
            getAttributeOrNull(literatureElement, "publisher"),
            getAttributeOrNull(literatureElement, "pubDate"),
            pages == null ? null : parsePages(pages),
            getAttributeOrNull(literatureElement, "href"),
            getAttributeOrNull(literatureElement, "browseDate"));

        registerReferable(literature);
        return literature;
    }

    private static final Pattern PAGE_RANGE_PATTERN = Pattern.compile("([0-9]+)(?:\\-([0-9]+))?");

    private static List<PageRange> parsePages(String pages) throws InputToIrTransformException {
        List<PageRange> pageRanges = new ArrayList<>();
        Matcher matcher = PAGE_RANGE_PATTERN.matcher(pages);
        while (matcher.find()) {
            String startString = matcher.group(1);
            String endString = matcher.group(2);
            int start, end;
            try {
                start = Integer.parseInt(startString);
                end = endString == null ? start
                    : Integer.parseInt(endString);
            } catch (NumberFormatException e) {
                throw new InputToIrTransformException(String.format("ページ範囲 %s を読み取れませんでした。", matcher.group()));
            }

            PageRange range;
            try {
                range = new PageRange(start, end);
            } catch (IllegalArgumentException e) {
                throw new InputToIrTransformException(String.format("ページ範囲 %s は正しい範囲ではありません。", matcher.group()));
            }

            pageRanges.add(range);
        }
        return pageRanges;
    }

    private static Stream<Node> toStream(NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength())
            .mapToObj(nodeList::item);
    }

    private static Stream<Element> childElements(Node node) {
        return toStream(node.getChildNodes())
            .filter(x -> x.getNodeType() == Node.ELEMENT_NODE)
            .map(x -> (Element) x);
    }

    private static Stream<Element> childElements(Node node, String name) {
        return childElements(node).filter(x -> x.getTagName().equals(name));
    }

    private static Element childElement(Node node, String name) {
        return childElements(node, name).findFirst().orElse(null);
    }

    private static String getAttributeOrNull(Element element, String attributeName) {
        return element.hasAttribute(attributeName)
            ? element.getAttribute(attributeName) : null;
    }

    private static final Pattern REMOVE_WHITE_SPACE_PATTERN = Pattern.compile("(?<=\\P{InCJK Symbols and Punctuation})\\s+(?=[\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}])");
    private static final Pattern UNIFY_WHITE_SPACE_PATTERN = Pattern.compile("\\s+");

    private static String normalizeText(String text, boolean trimStart) {
        if (trimStart) {
            // 最初の空白を消す
            int notWhiteSpaceIndex = 0;
            while (notWhiteSpaceIndex < text.length()
                && Character.isWhitespace(text.charAt(notWhiteSpaceIndex)))
                notWhiteSpaceIndex++;
            text = text.substring(notWhiteSpaceIndex);
        }

        // 「、」や「。」の後に空白があり、その後が日本語の場合は削除する
        text = REMOVE_WHITE_SPACE_PATTERN.matcher(text).replaceAll("");

        // スペースでない空白や、2個以上連続する空白は1個のスペースにする
        text = UNIFY_WHITE_SPACE_PATTERN.matcher(text).replaceAll(" ");

        return text;
    }
}
