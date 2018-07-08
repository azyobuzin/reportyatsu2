package reportyatsu2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.ir.Block;
import reportyatsu2.ir.FigureBlock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

import static reportyatsu2.OdfUtils.*;

public class IrToOdfPackageTransformer {
    private final boolean debugMode;
    private final Document contentDocument;
    private final Element automaticStylesElement;
    private final Element textElement;
    private final List<AdditionalFileInfo> additionalFiles = new ArrayList<>();

    public IrToOdfPackageTransformer(boolean debugMode) {
        this.debugMode = debugMode;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setIgnoringComments(true); // 作業用のコメントを捨てる
        documentBuilderFactory.setNamespaceAware(true); // 名前空間を使用する

        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError("DocumentBuilder を作成できませんでした。", e);
        }

        // テンプレートを読み込んで DOM を構築し、必要な Element を取り出しておく
        try {
            contentDocument = documentBuilder.parse(Resources.getResourceStream(Resources.CONTENT_TEMPLATE));
        } catch (Exception e) {
            throw new AssertionError("contentTemplate.xml が壊れています。", e);
        }

        automaticStylesElement = (Element) contentDocument
            .getElementsByTagNameNS(NS_OFFICE, "automatic-styles").item(0);
        assert automaticStylesElement != null;

        textElement = (Element) contentDocument
            .getElementsByTagNameNS(NS_OFFICE, "text").item(0);
        assert textElement != null;
    }

    public void inputIr(InputToIrTransformResult irResult) {
        // 画像ファイルを登録
        registerFigures(irResult);

        // 最大の深さがわかったので、箇条書きのスタイルを設定
        applyUnorderedListStyle(irResult);
        applyOrderedListStyle(irResult);

        // 各ブロックから ODF 形式の XML を生成
        transformIrToContent(irResult);
    }

    public void writeToStream(OutputStream stream) throws IOException, TransformerException {
        ZipOutputStream zip = new ZipOutputStream(stream);

        // 一番初めに mimetype を書き込む
        writeMimeType(zip);
        // manifest.xml を書き込む
        writeManifest(zip);
        // content.xml を書き込む
        writeContent(zip);

        // styles.xml を書き込む
        try (InputStream stylesStream = Resources.getResourceStream(Resources.DEFAULT_STYLES)) {
            writeStream(zip, STYLES_XML_PATH, stylesStream);
        }

        // 画像ファイルを書き込む
        for (AdditionalFileInfo file : additionalFiles) {
            try (InputStream fileStream = Files.newInputStream(file.getRealFilePath())) {
                writeStream(zip, file.getPathInPackage(), fileStream);
            }
        }

        zip.finish();
    }

    private void registerFigures(InputToIrTransformResult irResult) {
        for (Block block : irResult.getBlocks()) {
            if (!(block instanceof FigureBlock)) continue;
            FigureBlock figure = (FigureBlock) block;
            additionalFiles.add(new AdditionalFileInfo(
                figure.getRealFilePath(), figure.getPathInPackage(), figure.getMimeType()));
        }
    }

    private final float BASE_LIST_MARGIN_IN_CM = 0.635f;
    private final String[] BULLETS = {"•", "◦", "▪"};

    private void applyUnorderedListStyle(InputToIrTransformResult irResult) {
        int depth = irResult.getMaxUnorderedListDepth();
        if (depth <= 0) return;

        Element listStyle = contentDocument.createElementNS(NS_TEXT, "list-style");
        listStyle.setAttributeNS(NS_STYLE, "name", STYLE_UNORDERED_LIST);
        automaticStylesElement.appendChild(listStyle);

        for (int level = 1; level <= depth; level++) {
            Element levelStyle = contentDocument.createElementNS(NS_TEXT, "list-level-style-bullet");
            levelStyle.setAttributeNS(NS_TEXT, "level", Integer.toString(level));
            // BULLETS のパターンを繰り返し使う
            levelStyle.setAttributeNS(NS_TEXT, "bullet-char", BULLETS[(level - 1) % BULLETS.length]);

            levelStyle.appendChild(createListLevelProperties(level));
            listStyle.appendChild(levelStyle);
        }
    }

    private void applyOrderedListStyle(InputToIrTransformResult irResult) {
        int depth = irResult.getMaxOrderedListDepth();
        if (depth <= 0) return;

        Element listStyle = contentDocument.createElementNS(NS_TEXT, "list-style");
        listStyle.setAttributeNS(NS_STYLE, "name", STYLE_ORDERED_LIST);
        automaticStylesElement.appendChild(listStyle);

        for (int level = 1; level <= depth; level++) {
            Element levelStyle = contentDocument.createElementNS(NS_TEXT, "list-level-style-number");
            levelStyle.setAttributeNS(NS_TEXT, "level", Integer.toString(level));
            levelStyle.setAttributeNS(NS_STYLE, "num-suffix", ".");
            levelStyle.setAttributeNS(NS_STYLE, "num-format", "1");
            levelStyle.appendChild(createListLevelProperties(level));
            listStyle.appendChild(levelStyle);
        }
    }

    private Element createListLevelProperties(int level) {
        Element levelProperties = contentDocument.createElementNS(NS_STYLE, "list-level-properties");
        levelProperties.setAttributeNS(NS_TEXT, "list-level-position-and-space-mode", "label-alignment");

        Element labelAlignment = contentDocument.createElementNS(NS_STYLE, "list-level-label-alignment");
        labelAlignment.setAttributeNS(NS_TEXT, "label-followed-by", "listtab");
        labelAlignment.setAttributeNS(NS_TEXT, "list-tab-stop-position", String.format("%fcm", (level + 1) * BASE_LIST_MARGIN_IN_CM));
        labelAlignment.setAttributeNS(NS_FO, "margin-left", String.format("%fcm", level * BASE_LIST_MARGIN_IN_CM));

        levelProperties.appendChild(labelAlignment);
        return levelProperties;
    }

    private void transformIrToContent(InputToIrTransformResult irResult) {
        for (Block block : irResult.getBlocks()) {
            for (Node node : block.createNodes(contentDocument, irResult))
                textElement.appendChild(node);
        }
    }

    private void writeMimeType(ZipOutputStream zip) throws IOException {
        byte[] mimeTypeData = OPEN_DOCUMENT_TEXT_MIME_TYPE.getBytes("UTF-8");

        // mimetype を非圧縮で書き込む
        ZipEntry entry = new ZipEntry("mimetype");
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(mimeTypeData.length);
        entry.setCompressedSize(mimeTypeData.length);

        // STORED では CRC32 の計算も自分で行う必要がある
        var crc = new CRC32();
        crc.update(mimeTypeData);
        entry.setCrc(crc.getValue());

        zip.putNextEntry(entry);
        zip.write(mimeTypeData);
    }

    private void writeManifest(ZipOutputStream zip) throws IOException, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true); // 名前空間を使用する

        DocumentBuilder documentBuilder;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError("DocumentBuilderFactory に対して不適切な設定", e);
        }

        Document manifestDocument = documentBuilder.newDocument();

        Element root = manifestDocument.createElementNS(NS_MANIFEST, "manifest");
        root.setAttribute("xmlns:manifest", NS_MANIFEST);
        root.setAttributeNS(NS_MANIFEST, "version", "1.2");
        manifestDocument.appendChild(root);

        // ルート
        root.appendChild(createFileEntry(manifestDocument, "/", OPEN_DOCUMENT_TEXT_MIME_TYPE));
        // content.xml
        root.appendChild(createFileEntry(manifestDocument, "content.xml", OPEN_DOCUMENT_TEXT_MIME_TYPE));
        // styles.xml
        root.appendChild(createFileEntry(manifestDocument, "styles.xml", OPEN_DOCUMENT_TEXT_MIME_TYPE));

        // 画像ファイル
        for (AdditionalFileInfo file : additionalFiles)
            root.appendChild(createFileEntry(manifestDocument, file.getPathInPackage(), file.getMimeType()));

        logXml(MANIFEST_XML_PATH, manifestDocument);

        // 書き出し
        zip.putNextEntry(new ZipEntry(MANIFEST_XML_PATH));
        writeXmlToStream(manifestDocument, zip);
    }

    private Element createFileEntry(Document document, String fullPath, String mediaType) {
        Element fileEntry = document.createElementNS(NS_MANIFEST, "file-entry");
        fileEntry.setAttributeNS(NS_MANIFEST, "full-path", fullPath);
        fileEntry.setAttributeNS(NS_MANIFEST, "media-type", mediaType);
        return fileEntry;
    }

    private void writeContent(ZipOutputStream zip) throws IOException, TransformerException {
        logXml(CONTENT_XML_PATH, contentDocument);

        zip.putNextEntry(new ZipEntry(CONTENT_XML_PATH));
        writeXmlToStream(contentDocument, zip);
    }

    private void writeStream(ZipOutputStream zip, String path, InputStream inputStream) throws IOException {
        zip.putNextEntry(new ZipEntry(path));
        inputStream.transferTo(zip);
    }

    private void writeXmlToStream(Document document, OutputStream stream) throws TransformerException {
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new AssertionError("TransformerFactory に対して不適切な設定", e);
        }

        transformer.transform(new DOMSource(document), new StreamResult(stream));
    }

    private void logXml(String name, Document document) {
        if (!debugMode) return;

        try {
            System.err.println(name);
            for (int i = 0; i < name.length(); i++)
                System.err.print('=');
            System.err.println();

            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            // 文字コードを問題にしないで済むように、一度 String にする
            String xmlString;
            try (Writer writer = new CharArrayWriter()) {
                transformer.transform(new DOMSource(document), new StreamResult(writer));
                xmlString = writer.toString();
            }

            System.err.println(xmlString);
        } catch (Exception e) {
            // ログなので、失敗しても特に問題はないので、エラーレポートだけして終了
            e.printStackTrace();
        }
    }
}
