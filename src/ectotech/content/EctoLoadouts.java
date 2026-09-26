package ectotech.content;

import arc.struct.Seq;
import arc.struct.StringMap;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.world.blocks.storage.CoreBlock;

public class EctoLoadouts {
    public static Schematic basicSpark;

    /** Вызывать после EctoBlocks.load(). */
    public static void load() {
        basicSpark = new Schematic(Seq.with(new Stile(EctoBlocks.coreSpark, 1, 1, null, (byte)0)), new StringMap(), 3, 3);
        basicSpark.tags.put("name", "Spark");
    }

    /** Вызывать из ClientLoadEvent: к этому моменту Schematics уже загружены и список не перезапишется. */
    public static void register() {
        Seq<Schematic> list = Vars.schematics.getLoadouts((CoreBlock)EctoBlocks.coreSpark);
        if (!list.contains(basicSpark, true)) list.add(basicSpark);
    }
}