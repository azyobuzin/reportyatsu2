package reportyatsu2.ir;

import java.util.List;

public class TableRow {
    private final List<TableCell> cells;

    public TableRow(List<TableCell> cells) {
        this.cells = cells;
    }

    public List<TableCell> getCells() { return cells; }

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
