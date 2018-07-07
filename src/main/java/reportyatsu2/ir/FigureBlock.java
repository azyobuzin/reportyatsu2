package reportyatsu2.ir;

public class FigureBlock extends CaptionBlock {
    private final String path;
    private final double zoom;

    public FigureBlock(String id, int sequenceNumber, InlineElementList caption, String path, double zoom) {
        super(id, sequenceNumber, caption);
        this.path = path;
        this.zoom = zoom;
    }

    public String getPath() { return path; }

    public double getZoom() { return zoom; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("図").append(getSequenceNumber());

        InlineElementList caption = getCaption();
        if (caption != null) sb.append(": ").append(caption);

        return sb.append(
            String.format(" (%s, %f%%)", getPath(), getZoom() * 100.0)
        ).toString();
    }
}
