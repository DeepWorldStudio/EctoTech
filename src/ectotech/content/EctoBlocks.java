package ectotech.content;

import arc.struct.EnumSet;
import ectotech.world.blocks.utility.CoreBlockImitator;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.TargetPriority;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BuildVisibility;

import static mindustry.type.ItemStack.with;

public class EctoBlocks {
    public static Block

            //environment
            polishedMarble,

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
            coreSpark,
            coreSparkImitator

            //turrets

            //unit factories

            //payloads

            //logic

            //campaign

            ;

    public static void load(){

        polishedMarble = new Floor("polished-marble") {{
            autotile = true;
            blendGroup = this;
        }};

        coreSpark = new CoreBlock("core-spark"){{
            requirements(Category.effect, with(EctoItems.bismuth, 3000, EctoItems.zinc, 3000, Items.silicon, 2000));
            alwaysUnlocked = true;

            isFirstTier = true;
            unitType = EctoUnitTypes.glare;
            health = 1200;
            itemCapacity = 5000;
            size = 3;

            unitCapModifier = 8;
            buildCostMultiplier = 0.8f;
            researchCostMultiplier = 0.25f;

        }};

        coreSparkImitator = new CoreBlockImitator("core-spark-imitator", (CoreBlock) coreSpark){{
            requirements(Category.effect, with(EctoItems.bismuth, 900, EctoItems.zinc, 450, Items.silicon, 2000));

            health = 1200;
            itemCapacity = 500;

        }};
    }
}