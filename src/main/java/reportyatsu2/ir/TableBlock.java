package reportyatsu2.ir;

import java.util.List;
import java.util.stream.Collectors;

public class TableBlock extends CaptionBlock {
    private final List<TableRow> rows;

    public TableBlock(String id, Integer sequenceNumber, InlineElementList caption, List<TableRow> rows) {
        super(id, sequenceNumber, caption);
        this.rows = rows;
    }

    public List<TableRow> getRows() { return rows; }

    @Override
    public String toString() {
        return getRows().stream().map(TableRow::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }
}
