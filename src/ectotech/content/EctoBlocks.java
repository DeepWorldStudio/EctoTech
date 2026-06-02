package ectotech.content;

import arc.Core;
import ectotech.world.blocks.distribution.PneumaticDuct;
import ectotech.world.blocks.distribution.PneumaticDuctBridge;
import ectotech.world.blocks.distribution.PneumaticDuctRouter;
import ectotech.world.blocks.distribution.PneumaticOverflowDuct;
import ectotech.world.blocks.utility.CoreBlockImitator;
import ectotech.world.gen.RegionSlicer;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.type.ItemStack.with;

public class EctoBlocks {
    public static Block

            //environment
            malachite, charoit, ebonite,
            polishedMarble,

            malachiteWall, charoitWall, eboniteWall,

            //boulders

            //colored floor and etc tiles

            //ores

            //wall ores

            //crafting

            //sandbox

            //walls

            //defense utils (regen and etc)

            //transport
            pneumaticDuct, armoredPneumaticDuct, pneumaticDuctRouter, pneumaticDuctBridge, pneumaticOverflowDuct, pneumaticUnderflowDuct,

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

    public static void load() {

        malachite = new Floor("malachite") {{
            variants = 4;
        }};

        charoit = new Floor("charoit") {{
            variants = 4;
        }};

        ebonite = new Floor("ebonite") {{
            variants = 4;
        }};

        polishedMarble = new Floor("polished-marble") {
            {
                autotile = true;
                drawEdgeOut = false;
                drawEdgeIn = false;
            }

            //public TextureRegion[] spriteRegions;

            @Override
            public void load() {
                super.load();
                autotileRegions = RegionSlicer.splitRegion(Core.atlas.find(name + "-autotile"), 32/*Это ширина одного спрайта*/, 32/*Высота*/, 0/*Отступ. Нужен чтобы между текстурами не было "разлома"*/);

                //spriteRegions[0/*номер спрайта*/] - выбор текстуры

            }
        };

        malachiteWall = new StaticWall("malachite-wall");

        charoitWall = new StaticWall("charoit-wall");

        eboniteWall = new StaticWall("ebonite-wall");

        pneumaticDuct = new PneumaticDuct("pneumatic-duct") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 1));
            health = 90;
            speed = 3f;
            researchCost = with(EctoItems.bismuth, 15);

            operatingPressure = 1f;
            thresholdPressure = 2.8f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.3f;
            maxEfficiencyCoeff = 1.33f;
            outflowTanhFactor = 11.5f;
            outflowExponentCoefficient = 2.8f;
            criticalPressure = 7f;
            superCriticalPressure = 12f;

            explodesOnSuperCritical = true;
        }};

        armoredPneumaticDuct = new PneumaticDuct("armored-pneumatic-duct") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 2)); // TODO: +chromium
            health = 180;
            speed = 3f;
            armored = true;
            researchCost = with(EctoItems.bismuth, 150); // TODO: +chromium

            operatingPressure = 1f;
            thresholdPressure = 2.8f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.3f;
            maxEfficiencyCoeff = 1.33f;
            outflowTanhFactor = 11.5f;
            outflowExponentCoefficient = 2.8f;
            criticalPressure = 7f;
            superCriticalPressure = 12f;

            explodesOnSuperCritical = true;
        }};

        pneumaticDuctRouter = new PneumaticDuctRouter("pneumatic-duct-router") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 3));
            health = 180;
            speed = 3f;
            researchCost = with(EctoItems.bismuth, 30);

            operatingPressure = 1f;
            thresholdPressure = 2.8f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.3f;
            maxEfficiencyCoeff = 1.33f;
            outflowTanhFactor = 11.5f;
            outflowExponentCoefficient = 2.8f;
            criticalPressure = 7f;
            superCriticalPressure = 12f;

            explodesOnSuperCritical = true;
        }};

        pneumaticDuctBridge = new PneumaticDuctBridge("pneumatic-duct-bridge") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 3));
            health = 180;
            speed = 3f;
            researchCost = with(EctoItems.bismuth, 30);

            operatingPressure = 1f;
            thresholdPressure = 2.8f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.3f;
            maxEfficiencyCoeff = 1.33f;
            outflowTanhFactor = 11.5f;
            outflowExponentCoefficient = 2.8f;
            criticalPressure = 7f;
            superCriticalPressure = 12f;

            explodesOnSuperCritical = true;
        }};

        pneumaticOverflowDuct = new PneumaticOverflowDuct("pneumatic-overflow-duct") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 3));
            health = 180;
            speed = 3f;
            researchCost = with(EctoItems.bismuth, 30);

            operatingPressure = 1f;
            thresholdPressure = 2.8f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.3f;
            maxEfficiencyCoeff = 1.33f;
            outflowTanhFactor = 11.5f;
            outflowExponentCoefficient = 2.8f;
            criticalPressure = 7f;
            superCriticalPressure = 12f;

            explodesOnSuperCritical = true;
        }};

        pneumaticUnderflowDuct = new PneumaticOverflowDuct("pneumatic-underflow-duct") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 3));
            health = 180;
            speed = 3f;
            invert = true;
            researchCost = with(EctoItems.bismuth, 30);

            operatingPressure = 1f;
            thresholdPressure = 2.8f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.3f;
            maxEfficiencyCoeff = 1.33f;
            outflowTanhFactor = 11.5f;
            outflowExponentCoefficient = 2.8f;
            criticalPressure = 7f;
            superCriticalPressure = 12f;

            explodesOnSuperCritical = true;
        }};

        coreSpark = new CoreBlock("core-spark") {{
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

        coreSparkImitator = new CoreBlockImitator("core-spark-imitator", (CoreBlock) coreSpark) {{
            requirements(Category.effect, with(EctoItems.bismuth, 900, EctoItems.zinc, 450, Items.silicon, 2000));

            health = 1200;
            itemCapacity = 500;

        }};
    }
}