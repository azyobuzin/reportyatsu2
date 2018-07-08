package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static reportyatsu2.OdfUtils.*;

public class TableBlock extends CaptionBlock {
    private final List<TableRow> rows;

    public TableBlock(String id, Integer sequenceNumber, InlineElementList caption, List<TableRow> rows) {
        super(id, sequenceNumber, caption);
        this.rows = rows;
    }

    public List<TableRow> getRows() { return rows; }

    @Override
    protected String getSequenceName() { return "Table"; }

    @Override
    protected String getSequenceDisplayName() { return "表"; }

    public int getColumnCount() {
        return getRows().stream()
            .mapToInt(row -> row.getCells().size())
            .max().orElse(0);
    }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        Element table = document.createElementNS(NS_TABLE, "table:table");
        table.setAttributeNS(NS_TABLE, "table:style-name", STYLE_STANDARD_TABLE);

        int columnCount = getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            Element column = document.createElementNS(NS_TABLE, "table:table-column");
            table.appendChild(column);
        }

        for (TableRow row : getRows())
            table.appendChild(row.createElement(document, irResult));

        return Arrays.asList(createCaptionParagraph(document, irResult), table);
    }

    @Override
    public String toString() {
        return getRows().stream().map(TableRow::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }
}
