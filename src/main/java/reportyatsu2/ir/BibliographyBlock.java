package reportyatsu2.ir;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import reportyatsu2.InputToIrTransformResult;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static reportyatsu2.OdfUtils.*;

public class BibliographyBlock extends Block {
    private final List<Literature> literatureList;

    public BibliographyBlock(List<Literature> literatureList) {
        this.literatureList = literatureList;
    }

    public List<Literature> getLiteratureList() { return literatureList; }

    @Override
    public List<Node> createNodes(Document document, InputToIrTransformResult irResult) {
        Element list = document.createElementNS(NS_TEXT, "text:list");
        list.setAttributeNS(NS_TEXT, "text:style-name", STYLE_BIBLIOGRAPHY_LIST);

        for (Literature literature : getLiteratureList())
            list.appendChild(literature.createElement(document));

        return Collections.singletonList(list);
    }

    @Override
    public String toString() {
        return getLiteratureList().stream()
            .map(Literature::toString)
            .collect(Collectors.joining(System.lineSeparator()));
    }
}
