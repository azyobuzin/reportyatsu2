package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.Arrays;
import java.util.List;

import static reportyatsu2.OdfUtils.*;

public class CodeBlock extends CaptionBlock {
    private final String language;
    private final String code;

    public CodeBlock(String id, int sequenceNumber, InlineElementList caption, String language, String code) {
        super(id, sequenceNumber, caption);
        this.language = language;
        this.code = code;
    }

    public String getLanguage() { return language; }

    public String getCode() { return code; }

    @Override
    protected String getSequenceName() { return "リスト"; }

    @Override
    protected String getSequenceDisplayName() { return "リスト"; }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        Element table = document.createElementNS(NS_TABLE, "table:table");
        table.setAttributeNS(NS_TABLE, "table:style-name", STYLE_CODE_BLOCK_TABLE);

        Element lineNumberColumn = document.createElementNS(NS_TABLE, "table:table-column");
        lineNumberColumn.setAttributeNS(NS_TABLE, "table:style-name", STYLE_CODE_BLOCK_LINE_NUMBER_COLUMN);
        table.appendChild(lineNumberColumn);

        Element codeColumn = document.createElementNS(NS_TABLE, "table:table-column");
        codeColumn.setAttributeNS(NS_TABLE, "table:style-name", STYLE_CODE_BLOCK_CODE_COLUMN);
        table.appendChild(codeColumn);

        // 行ごとに処理
        String[] lines = getCode().split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String lineNumberCellStyle;
            String codeCellStyle;
            if (lines.length == 1) {
                // 1行しかない場合のスタイル
                lineNumberCellStyle = STYLE_CODE_BLOCK_LINE_NUMBER_CELL_ONE_LINE;
                codeCellStyle = STYLE_CODE_BLOCK_CODE_CELL_ONE_LINE;
            } else if (i == 0) {
                // 1行目のスタイルを適用
                lineNumberCellStyle = STYLE_CODE_BLOCK_LINE_NUMBER_CELL_FIRST;
                codeCellStyle = STYLE_CODE_BLOCK_CODE_CELL_FIRST;
            } else if (i == lines.length - 1) {
                // 最後の行のスタイルを適用
                lineNumberCellStyle = STYLE_CODE_BLOCK_LINE_NUMBER_CELL_LAST;
                codeCellStyle = STYLE_CODE_BLOCK_CODE_CELL_LAST;
            } else {
                // 中間の行のスタイルを適用
                lineNumberCellStyle = STYLE_CODE_BLOCK_LINE_NUMBER_CELL;
                codeCellStyle = STYLE_CODE_BLOCK_CODE_CELL;
            }

            Element row = document.createElementNS(NS_TABLE, "table:table-row");
            table.appendChild(row);

            // 行番号セル
            Element lineNumberCell = document.createElementNS(NS_TABLE, "table:table-cell");
            lineNumberCell.setAttributeNS(NS_TABLE, "table:style-name", lineNumberCellStyle);
            row.appendChild(lineNumberCell);

            Element lineNumberParagraph = createParagraphElement(document, STYLE_CODE_BLOCK_LINE_NUMBER);
            lineNumberParagraph.appendChild(document.createTextNode(Integer.toString(i + 1)));
            lineNumberCell.appendChild(lineNumberParagraph);

            // コードセル
            Element codeCell = document.createElementNS(NS_TABLE, "table:table-cell");
            codeCell.setAttributeNS(NS_TABLE, "table:style-name", codeCellStyle);
            row.appendChild(codeCell);

            Element codeParagraph = createParagraphElement(document, STYLE_CODE_BLOCK_CODE);
            codeCell.appendChild(codeParagraph);
            for (Node node : createNodesForText(document, lines[i]))
                codeParagraph.appendChild(node);
        }

        return Arrays.asList(createCaptionParagraph(document, irResult), table);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("リスト")
            .append(getSequenceNumber());

        InlineElementList caption = getCaption();
        if (caption != null)
            sb.append(": ").append(caption);

        return sb.append(System.lineSeparator()).append("```")
            .append(getLanguage()).append(System.lineSeparator())
            .append(getCode()).append(System.lineSeparator())
            .append("```").toString();
    }
}
