package reportyatsu2.ir;

public class FigureBlock extends CaptionBlock {
    private final String pathInPackage;
    private final double zoom;

    public FigureBlock(String id, Integer sequenceNumber, InlineElementList caption, String pathInPackage, double zoom) {
        super(id, sequenceNumber, caption);
        this.pathInPackage = pathInPackage;
        this.zoom = zoom;
    }

    public String getPathInPackage() { return pathInPackage; }

    public double getZoom() { return zoom; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("図");
        Integer seqNum = getSequenceNumber();
        if (seqNum != null) sb.append(seqNum);
        InlineElementList caption = getCaption();
        if (caption != null) sb.append(": ").append(caption);
        sb.append(String.format(" (%s, %f%%)", getPathInPackage(), getZoom() * 100.0));
        return sb.toString();
    }
}
