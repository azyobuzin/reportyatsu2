package reportyatsu2.ir;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;
import reportyatsu2.InputToIrTransformException;

import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static reportyatsu2.OdfUtils.*;

public class FigureBlock extends CaptionBlock {
    private final double DEFAULT_DPI = 72.0;

    private final String path;
    private final Path realFilePath;
    private final String pathInPackage;
    private final String mimeType;
    private final double displayWidthInInch;
    private final double displayHeightInInch;

    public FigureBlock(String id, int sequenceNumber, InlineElementList caption, Path workingDirectory, String path, double zoom) throws InputToIrTransformException {
        super(id, sequenceNumber, caption);
        this.path = path;

        try {
            // 絶対パスの取得
            realFilePath = workingDirectory != null
                ? workingDirectory.resolve(path)
                : FileSystems.getDefault().getPath(path);
        } catch (InvalidPathException e) {
            throw new InputToIrTransformException(String.format("パス '%s' を読み取れませんでした: %s", path, e.getMessage()), e);
        }
        // 画像ファイルの解析
        ImageInfo imageInfo;
        try {
            imageInfo = Imaging.getImageInfo(realFilePath.toFile());
        } catch (Exception e) {
            throw new InputToIrTransformException(String.format("画像 '%s' の読み取りに失敗しました: %s", path, e.getMessage()), e);
        }

        double imageWidthInInch = imageInfo.getPhysicalWidthInch();
        if (imageWidthInInch < 0)
            imageWidthInInch = imageInfo.getWidth() / DEFAULT_DPI;
        displayWidthInInch = imageWidthInInch * zoom;

        double imageHeightInInch = imageInfo.getPhysicalHeightInch();
        if (imageHeightInInch < 0)
            imageHeightInInch = imageInfo.getHeight() / DEFAULT_DPI;
        displayHeightInInch = imageHeightInInch * zoom;

        // ファイル形式がわかったので、パッケージ内でのファイル名を確定する
        pathInPackage = String.format("figure%d.%s", sequenceNumber, imageInfo.getFormat().getExtension());
        mimeType = imageInfo.getMimeType();
    }

    public String getPath() { return path; }

    public Path getRealFilePath() { return realFilePath;}

    public String getPathInPackage() { return pathInPackage; }

    public String getMimeType() { return mimeType; }

    public double getDisplayWidthInInch() { return displayWidthInInch; }

    public double getDisplayHeightInInch() { return displayHeightInInch; }

    @Override
    protected String getSequenceName() { return "Illustration"; }

    @Override
    protected String getSequenceDisplayName() { return "図"; }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        Element paragraph = createParagraphElement(document, STYLE_IMAGE_BLOCK);

        Element frame = document.createElementNS(NS_DRAW, "draw:frame");
        frame.setAttributeNS(NS_TEXT, "text:anchor-type", "as-char"); // 行内
        frame.setAttributeNS(NS_SVG, "svg:width", String.format("%fin", getDisplayWidthInInch()));
        frame.setAttributeNS(NS_SVG, "svg:height", String.format("%fin", getDisplayHeightInInch()));
        paragraph.appendChild(frame);

        Element image = document.createElementNS(NS_DRAW, "draw:image");
        image.setAttributeNS(NS_XLINK, "xlink:href", getPathInPackage());
        image.setAttributeNS(NS_XLINK, "xlink:type", "simple");
        image.setAttributeNS(NS_XLINK, "xlink:show", "embed");
        image.setAttributeNS(NS_XLINK, "xlink:actuate", "onLoad");
        frame.appendChild(image);

        // 改行してキャプションを追加
        paragraph.appendChild(createLineBreakElement(document));

        for (Node node : createCaptionContentNodes(document, irResult))
            paragraph.appendChild(node);

        return Collections.singletonList(paragraph);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("図").append(getSequenceNumber());

        InlineElementList caption = getCaption();
        if (caption != null) sb.append(": ").append(caption);

        return sb.append(
            String.format(" (%s, %fin x %fin)", getPath(), getDisplayWidthInInch(), getDisplayHeightInInch())
        ).toString();
    }
}
