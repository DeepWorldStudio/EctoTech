package ectotech.content;

import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BuildVisibility;

import static mindustry.type.ItemStack.with;

public class EctoBlocks {
    public static Block

            //environment

            //boulders

            //colored floor and etc tiles

            //ores

            //wall ores

            //crafting

            //sandbox

            //walls

            //defense utils (regen and etc)

            //transport

            //liquid

            //power

            //production (drills and bores)

            //cores and storages
            coreSpark

            //turrets

            //unit factories

            //payloads

            //logic

            //campaign

            ;

    public static void load() {


        //endregion
        //region cores and storages

        coreSpark = new CoreBlock("core-spark") {{
            requirements(Category.effect, BuildVisibility.coreZoneOnly, with(EctoItems.bismuth, 2500, EctoItems.zinc, 1800));
            alwaysUnlocked = true;

            isFirstTier = true;
            unitType = UnitTypes.alpha;
            health = 1100;
            itemCapacity = 4000;
            size = 3;
            buildCostMultiplier = 2f;

            unitCapModifier = 18;
        }};
    }
}

