package reportyatsu2.ir;

import java.util.List;
import java.util.stream.Collectors;

public class BibliographyBlock extends Block {
    private final List<Literature> literatureList;

    public BibliographyBlock(List<Literature> literatureList) {
        this.literatureList = literatureList;
    }

    public List<Literature> getLiteratureList() { return literatureList; }

    @Override
    public String toString() {
        return getLiteratureList().stream()
            .map(Literature::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }
}
