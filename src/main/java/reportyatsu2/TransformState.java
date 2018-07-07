package reportyatsu2;

import reportyatsu2.ir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TransformState {
    private final List<Block> blocks = new ArrayList<>();
    private final Map<String, Referable> idMap = new HashMap<>();

    public void registerReferableElement(Referable element) {
        String id = element.getId();
        if (id != null) idMap.put(id, element);
    }

    public Referable getElementById(String id) {
        return idMap.get(id);
    }

    public List<Block> getBlocks() { return blocks; }

    public void addBlock(Block block) { getBlocks().add(block); }
}
