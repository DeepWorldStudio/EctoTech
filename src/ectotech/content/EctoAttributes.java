package ectotech.content;

import mindustry.world.meta.Attribute;

public class EctoAttributes {

    public static Attribute geyser, sulfur, teynorite;

    public static void load() {
        geyser = Attribute.add("ectotech-geyser");
        sulfur = Attribute.add("ectotech-sulfur");
        teynorite = Attribute.add("ectotech-teynorite");
    }

}
