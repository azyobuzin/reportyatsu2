package reportyatsu2.ir;

import java.util.ArrayList;

public class InlineElementList extends ArrayList<InlineElement> {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (InlineElement e : this)
            sb.append(e);
        return sb.toString();
    }
}
