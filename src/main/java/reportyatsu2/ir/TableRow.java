package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import reportyatsu2.InputToIrTransformResult;

import java.util.List;

import static reportyatsu2.OdfUtils.*;

public class TableRow {
    private final List<TableCell> cells;

    public TableRow(List<TableCell> cells) {
        this.cells = cells;
    }

    public List<TableCell> getCells() { return cells; }

    public Element createElement(Document document, InputToIrTransformResult irResult) {
        Element row = document.createElementNS(NS_TABLE, "table-row");
        for (TableCell cell : getCells())
            row.appendChild(cell.createElement(document, irResult));
        return row;
    }

    @Override
    public String toString() {
        List<TableCell> cells = getCells();
        if (cells.size() == 0) return "";

        // | cell | cell | の形で出力
        StringBuilder sb = new StringBuilder("|");
        for (TableCell cell : cells)
            sb.append(" ").append(cell).append(" |");

        return sb.toString();
    }
}
