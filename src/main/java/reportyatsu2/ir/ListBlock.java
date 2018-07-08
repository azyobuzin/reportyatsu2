package reportyatsu2.ir;

import java.util.Arrays;

public class ListBlock extends Block {
    private final boolean ordered;
    private final ListOfItems list;

    public ListBlock(boolean ordered, ListOfItems list) {
        this.ordered = ordered;
        this.list = list;
    }

    public boolean isOrdered() { return ordered; }

    public ListOfItems getList() { return list; }

    public int getDepth() { return getDepth(getList()); }

    private static int getDepth(ListOfItems list) {
        int depth = 0;
        for (ListItem item : list) {
            ListOfItems childList = item.getChildList();
            if (childList != null)
                depth = Math.max(depth, getDepth(childList));
        }
        return depth + 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendList(sb, getList(), 0);

        // 最後の \r\n を削除
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);

            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\r')
                sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    private void appendList(StringBuilder sb, ListOfItems list, int indent) {
        // インデントの空白を作成
        final int INDENT_SIZE = 2;
        char[] spaceChars = new char[indent * INDENT_SIZE];
        Arrays.fill(spaceChars, ' ');
        String indentString = new String(spaceChars);

        for (int i = 0; i < list.size(); i++) {
            ListItem item = list.get(i);

            // 連番（点）を出力
            sb.append(indentString);
            sb.append(isOrdered() ? (Integer.toString(i + 1) + ". ") : "* ");

            // 内容を出力
            sb.append(item.getInlineElements()).append(System.lineSeparator());

            // 子リストがあるならば、インデントを増やして出力
            ListOfItems child = item.getChildList();
            if (child != null) appendList(sb, child, indent + 1);
        }
    }
}
