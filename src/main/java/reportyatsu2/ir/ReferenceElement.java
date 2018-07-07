package reportyatsu2.ir;

public class ReferenceElement extends InlineElement {
    private final String targetId;

    public ReferenceElement(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetId() { return targetId; }

    @Override
    public String toString() {
        return String.format("[ref:%s]", getTargetId());
    }
}
