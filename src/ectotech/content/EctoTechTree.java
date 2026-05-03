package ectotech.content;

import static ectotech.content.EctoBlocks.coreSpark;
import static mindustry.content.TechTree.nodeRoot;

public class EctoTechTree {
    public static void load() {
        EctoPlanets.ectorum.techTree = nodeRoot("ectorum", coreSpark, () -> {
        });
    }
}
