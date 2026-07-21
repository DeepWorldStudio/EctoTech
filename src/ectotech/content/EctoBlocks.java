package ectotech.content;

import arc.Core;
import arc.graphics.Color;
import ectotech.world.blocks.distribution.PneumaticDuct;
import ectotech.world.blocks.distribution.PneumaticDuctBridge;
import ectotech.world.blocks.distribution.PneumaticDuctRouter;
import ectotech.world.blocks.distribution.PneumaticOverflowDuct;
import ectotech.world.blocks.environment.EctorumQuicksand;
import ectotech.world.blocks.environment.SteamGeyser;
import ectotech.world.blocks.power.GeyserGenerator;
import ectotech.world.blocks.production.MultipleWallCrafter;
import ectotech.world.blocks.production.PressurizedCrafter;
import ectotech.world.blocks.utility.CoreBlockImitator;
import ectotech.world.draw.DrawCompoundAssembly;
import ectotech.world.gen.RegionSlicer;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.gen.Sounds;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.draw.*;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;

import static mindustry.type.ItemStack.with;

public class EctoBlocks {
    public static Block

            //natural environment
            malachite, charoit, ebonite,

            crushedClivelite, clivelite, smoothClivelite,
            ectorumSand, ectorumQuicksand, ectorumSandstone,
            kyanicStoneNugget, kyanicStone,

            // handmade environment
            polishedMarble,

            // geysers
            cliveliteGeyser,

            // envWalls
            malachiteWall, charoitWall, eboniteWall, ectorumSandstoneWall, cliveliteWall, kyanicStoneWall,

            // boulders and custom env decorations

            // colored floor and etc tiles

            // ores
            oreBismuth, oreZinc, oreLithium, oreCoralThorium, oreBorum,

            // wall ores
            wallOreBismuth, wallOreZinc, wallOreLithium,  wallOreCoralThorium, kyaniteWall,

            // crafting
            highPressureSiliconSmelter, compoundAssembler,

            // sandbox
            pressureSource, pressureVoid,

            // walls
            bismuthWall, bismuthWallLarge,

            // defense utils (regen and etc)

            // transport
            pneumaticDuct, armoredPneumaticDuct, pneumaticDuctRouter, pneumaticDuctBridge, pneumaticOverflowDuct, pneumaticUnderflowDuct,

            // liquid

            // power
            geyserGenerator,

            // production (drills and bores)
            impulseBore, largeImpulseBore, highPrecisionDrill, rotorDrill, vacuumDrill, magneticDrill,
            cliffShredder, crusher,

            // cores, imitators and storages
            coreSpark, coreSunrise,
            coreSparkImitator, coreSunriseImitator

            // turrets

            // unit factories

            // payloads

            // logic

            // campaign

            ;

    public static void load() {

        // natural environment

        malachite = new Floor("malachite") {{
            variants = 4;
        }};

        charoit = new Floor("charoit") {{
            variants = 4;
        }};

        ebonite = new Floor("ebonite") {{
            variants = 4;
        }};

        smoothClivelite = new Floor("smooth-clivelite") {{
            variants = 3;
        }};

        clivelite = new Floor("clivelite") {{
            variants = 3;
        }};

        crushedClivelite = new Floor("crushed-clivelite") {{
            variants = 7;
        }};

        ectorumSand = new Floor("ectorum-sand") {{
            variants = 4;
            itemDrop = Items.sand;

            playerUnmineable = true;
        }};

        ectorumQuicksand = new EctorumQuicksand("ectorum-quicksand") {{
            speedMultiplier = 0.35f;
            dragMultiplier = 2.25f;

            status = EctoStatusEffects.quicksandStuck;
            statusDuration = 120f;
            buildingDamageTaken = 0.5f;
            damageTaken = 1.25f;
            drownTime = 450f;

            albedo = 0.05f;

            variants = 0;
            cacheLayer = EctoShaders.quicksandHeat;
            supportsOverlay = true;
        }};

        ectorumSandstone = new Floor("ectorum-sandstone") {{
            variants = 3;
            itemDrop = Items.sand;

            playerUnmineable = true;
        }};

        kyanicStone = new Floor("kyanic-stone") {{
            variants = 3;
        }};

        kyanicStoneNugget = new Floor("kyanic-stone-nugget") {{
            variants = 3;
        }};

        // handmade environment

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

        // geysers

        cliveliteGeyser = new SteamGeyser("clivelite-geyser") {{
            parent = blendGroup = clivelite;
            attributes.set(Attribute.steam, 1f);
            variants = 3;
            activeTime = 8f * 60f;
            passiveTime = 24f * 60f;
            unitDamageTaken = 1.58f;
        }};

        // envWalls

        malachiteWall = new StaticWall("malachite-wall") {{
            malachite.asFloor().wall = this;
        }};

        charoitWall = new StaticWall("charoit-wall") {{
            charoit.asFloor().wall = this;
        }};

        eboniteWall = new StaticWall("ebonite-wall") {{
            ebonite.asFloor().wall = this;
        }};

        ectorumSandstoneWall = new StaticWall("ectorum-sandstone-wall") {{
            ectorumSand.asFloor().wall = ectorumQuicksand.asFloor().wall = ectorumSandstone.asFloor().wall = this;
            attributes.set(Attribute.sand, 1f);
        }};

        cliveliteWall = new StaticWall("clivelite-wall") {{
            clivelite.asFloor().wall = smoothClivelite.asFloor().wall = crushedClivelite.asFloor().wall = this;
        }};

        kyanicStoneWall = new StaticWall("kyanic-stone-wall") {{
            kyanicStoneNugget.asFloor().wall = kyanicStone.asFloor().wall = this;
        }};

        // boulders

        // colored floor and etc tiles

        // ore

        oreBismuth = new OreBlock("ore-bismuth", EctoItems.bismuth);

        oreZinc = new OreBlock("ore-zinc", EctoItems.zinc);

        oreLithium = new OreBlock("ore-lithium", EctoItems.lithium);

        // wall ores

        kyaniteWall = new StaticWall("kyanite-wall") {{
            itemDrop = EctoItems.kyanite;
            variants = 3;
        }};

        highPressureSiliconSmelter = new PressurizedCrafter("high-pressure-silicon-smelter") {{
            requirements(Category.crafting, with(EctoItems.bismuth, 45, Items.graphite, 30, EctoItems.zinc, 10));
            craftEffect = Fx.none;
            outputItem = new ItemStack(Items.silicon, 2);
            craftTime = 80f;
            size = 3;
            hasPower = true;
            hasLiquids = false;
            envEnabled |= Env.space | Env.underwater;
            envDisabled = Env.none;
            itemCapacity = 30;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(), new DrawDefault());
            fogRadius = 3;
            researchCost = with(EctoItems.bismuth, 400, Items.graphite, 250, EctoItems.zinc, 120);
            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.12f;

            operatingPressure = 1f;
            thresholdPressure = 0.1f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.91f;
            maxEfficiencyCoeff = 2.5f;
            outflowTanhFactor = 15f;
            outflowExponentCoefficient = 14f;
            criticalPressure = 14f;
            superCriticalPressure = 21f;

            consumeItems(with(Items.graphite, 2, Items.sand, 3));
            consumePower(180f / 60f);
        }};

        compoundAssembler = new GenericCrafter("compound-assembler") {{
            requirements(Category.crafting, with(EctoItems.bismuth, 30, Items.graphite, 10, Items.silicon, 45));
            craftEffect = Fx.none;
            outputItem = new ItemStack(EctoItems.hydrodefensiveCompound, 1);
            craftTime = 160f;
            size = 3;
            hasPower = true;
            hasLiquids = false;
            itemCapacity = 30;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawCompoundAssembly() {{
                        coreColor = Color.valueOf("7a9fb5");
                        ringRadius = 7f;
                        ringPulseScl = 2f;
                        ringPulseMag = 0.1f;
                    }},
                    new DrawCrucibleFlame() {{
                        flameColor =  Color.valueOf("918279");
                    }},
                    new DrawDefault());
            fogRadius = 3;
            researchCost = with(EctoItems.bismuth, 600, Items.graphite, 120, Items.silicon, 450);
            ambientSound = Sounds.loopMachine2;
            ambientSoundVolume = 0.12f;

            consumeItems(with(EctoItems.bismuth, 3, Items.silicon, 1));
            consumePower(120f / 60f);
        }};

        pneumaticDuct = new PneumaticDuct("pneumatic-duct") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 1));
            health = 80;
            speed = 7f;
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

        }};

        armoredPneumaticDuct = new PneumaticDuct("armored-pneumatic-duct") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 2, EctoItems.chromium, 1));
            health = 180;
            speed = 3f;
            armored = true;
            researchCost = with(EctoItems.bismuth, 150, EctoItems.chromium, 70);

            operatingPressure = 1f;
            thresholdPressure = 2.8f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.3f;
            maxEfficiencyCoeff = 1.33f;
            outflowTanhFactor = 11.5f;
            outflowExponentCoefficient = 2.8f;
            criticalPressure = 7f;
            superCriticalPressure = 12f;
        }};

        pneumaticDuctRouter = new PneumaticDuctRouter("pneumatic-duct-router") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 3));
            health = 55;
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
        }};

        pneumaticDuctBridge = new PneumaticDuctBridge("pneumatic-duct-bridge") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 7));
            health = 180;
            speed = 3f;
            buildCostMultiplier = 2f;
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
        }};

        geyserGenerator = new GeyserGenerator("geyser-generator") {{
            requirements(Category.power, with(EctoItems.bismuth, 40, Items.graphite, 15));
            size = 3;
            attribute = EctoAttributes.geyser;
            displayEfficiency = false;
            generateEffect = Fx.steam;
            effectChance = 0.011f;

            fogRadius = size;

            activeOutput = 70f / 60f;
            passiveOutput = 3f / 60f;
        }};

        cliffShredder = new MultipleWallCrafter("cliff-shredder") {{
            requirements(Category.production, with(EctoItems.bismuth, 20, EctoItems.zinc, 8));
            consumePower(11 / 60f);

            drillTime = 110f;
            size = 2;
            fogRadius = size;
            ambientSound = Sounds.loopDrill;
            ambientSoundVolume = 0.04f;

            wallAttributes.add(new MultipleWallCrafter.AttributeWall(Attribute.sand, Items.sand));
            wallAttributes.add(new MultipleWallCrafter.AttributeWall(EctoAttributes.sulfur, EctoItems.sulfur));
            wallAttributes.add(new MultipleWallCrafter.AttributeWall(EctoAttributes.teynorite, EctoItems.teynorite));
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
        }};

        coreSunrise = new CoreBlock("core-sunrise") {{
            requirements(Category.effect, with(EctoItems.bismuth, 8500, Items.silicon, 6000, EctoItems.chromium, 6000, EctoItems.sulfur, 1500));

            health = 2300;
            itemCapacity = 8000;
            size = 4;

            unitCapModifier = 14;
            buildCostMultiplier = 1.1f;
            researchCostMultiplier = 1.3f;
        }};

        coreSparkImitator = new CoreBlockImitator("core-spark-imitator", (CoreBlock) coreSpark) {{
            requirements(Category.effect, with(EctoItems.bismuth, 900, EctoItems.zinc, 450, Items.silicon, 450));

            health = 1200;
            itemCapacity = 500;

        }};
    }
}