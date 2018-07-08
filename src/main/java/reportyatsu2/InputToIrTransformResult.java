package reportyatsu2;

import reportyatsu2.ir.Block;
import reportyatsu2.ir.Referable;

import java.util.List;
import java.util.Map;

public interface InputToIrTransformResult {
    List<Block> getBlocks();

    Map<String, Referable> getIdMap();

    int getMaxSectionDepth();

    int getMaxUnorderedListDepth();

    int getMaxOrderedListDepth();
}
