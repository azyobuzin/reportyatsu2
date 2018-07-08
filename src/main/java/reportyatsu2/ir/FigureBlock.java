package reportyatsu2.ir;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import reportyatsu2.TransformException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class FigureBlock extends CaptionBlock {
    private final String path;
    private final Path realFilePath;
    private final String pathInPackage;
    private final String mimeType;
    private final double widthInInch;
    private final double heightInInch;

    public FigureBlock(String id, int sequenceNumber, InlineElementList caption, Path workingDirectory, String path, double zoom) throws TransformException {
        super(id, sequenceNumber, caption);
        this.path = path;

        try {
            // 絶対パスの取得
            realFilePath = workingDirectory.resolve(path);
        } catch (InvalidPathException e) {
            throw new TransformException(String.format("パス '%s' を読み取れませんでした: %s", path, e.getMessage()), e);
        }
        // 画像ファイルの解析
        ImageInfo imageInfo;
        try {
            imageInfo = Imaging.getImageInfo(realFilePath.toFile());
        } catch (Exception e) {
            throw new TransformException(String.format("画像 '%s' の読み取りに失敗しました: %s", path, e.getMessage()), e);
        }
        mimeType = imageInfo.getMimeType();
        widthInInch = imageInfo.getPhysicalWidthInch() * zoom;
        assert widthInInch > 0.0;
        heightInInch = imageInfo.getPhysicalHeightInch() * zoom;
        assert heightInInch > 0.0;
        // ファイル形式がわかったので、パッケージ内でのファイル名を確定する
        pathInPackage = String.format("figure%d.%s", sequenceNumber, imageInfo.getFormat().getExtension());
    }

    public String getPath() { return path; }

    public Path getRealFilePath() { return realFilePath;}

    public String getPathInPackage() { return pathInPackage; }

    public String getMimeType() { return mimeType; }

    public double getWidthInInch() { return widthInInch; }

    public double getHeightInInch() { return heightInInch; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("図").append(getSequenceNumber());

        InlineElementList caption = getCaption();
        if (caption != null) sb.append(": ").append(caption);

        return sb.append(
            String.format(" (%s, %fin x %fin)", getPath(), getWidthInInch(), getHeightInInch())
        ).toString();
    }
}
