package reportyatsu2;

import java.nio.file.Path;

public class AdditionalFileInfo {
    private final Path realFilePath;
    private final String pathInPackage;
    private final String mimeType;

    public AdditionalFileInfo(Path realFilePath, String pathInPackage, String mimeType) {
        this.realFilePath = realFilePath;
        this.pathInPackage = pathInPackage;
        this.mimeType = mimeType;
    }

    public Path getRealFilePath() { return realFilePath; }

    public String getPathInPackage() { return pathInPackage; }

    public String getMimeType() { return mimeType; }
}
