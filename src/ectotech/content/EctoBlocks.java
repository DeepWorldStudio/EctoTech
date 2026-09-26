package ectotech.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import ectotech.graphics.EctoPal;
import ectotech.world.aspects.HeatAspect;
import ectotech.world.aspects.PressureAspect;
import ectotech.world.blocks.defense.SelfRegenWallCasing;
import ectotech.world.blocks.defense.UnitSignatureRadar;
import ectotech.world.blocks.defense.turrets.AmmoTargetItemTurret;
import ectotech.world.blocks.distribution.PneumaticDuct;
import ectotech.world.blocks.distribution.PneumaticDuctBridge;
import ectotech.world.blocks.distribution.PneumaticDuctRouter;
import ectotech.world.blocks.distribution.PneumaticOverflowDuct;
import ectotech.world.blocks.environment.*;
import ectotech.world.blocks.liquid.IntegrityConduit;
import ectotech.world.blocks.liquid.IntegrityLiquidBridge;
import ectotech.world.blocks.liquid.IntegrityLiquidJunction;
import ectotech.world.blocks.liquid.IntegrityLiquidRouter;
import ectotech.world.blocks.power.GeyserGenerator;
import ectotech.world.blocks.power.OctoBeamNode;
import ectotech.world.blocks.production.*;
import ectotech.world.blocks.units.BranchReconstructor;
import ectotech.world.blocks.units.UpgradePlan;
import ectotech.world.blocks.utility.CoreBlockImitator;
import ectotech.world.draw.DrawCompoundAssembly;
import ectotech.world.gen.RegionSlicer;
import mindustry.content.*;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.RegionPart;
import mindustry.gen.Sounds;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.liquid.ArmoredConduit;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.power.Battery;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.draw.*;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Env;

import static mindustry.type.ItemStack.with;

public class EctoBlocks {

    public static Block

            // natural environment
            malachite,
            charoit, smoothCharoit, charoitCrater, charoitCraterBig,
            ebonite, smoothEbonite,
            smoothClivelite, clivelite, crushedClivelite,
            bluishMoss,
            ectorumSand, ectorumQuicksand, ectorumSandstone, boricSandWater,
            boricWater, deepBoricWater,
            sulfurFloor, smoothSulfurFloor, sulfurCrater, smoothSulfurFloorSubmerged, smoothSulfurFloorWater, sulfurSolution,
            boricIce,
            crackedMarble, smoothMarble,
            crackedThoriumite, thoriumite, smoothThoriumite,
            kyanicStone, kyanicStoneNugget,
            volcanicAsh, volcanicAshStone, muscovite, thermoplasmaFloor,

            // handmade environment
            polishedMarble,
            metalPlates1,

            // geysers
            charoitGeyser, eboniteGeyser, cliveliteGeyser, sulfurGeyser,

            // envWalls
            malachiteWall, charoitWall, eboniteWall, cliveliteWall, mossyCliveliteWall, bluishMossWall, ectorumSandstoneWall, sulfurWall, pyritedSulfurWall, boricIceWall, thoriumiteWall, kyanicStoneWall,
            polishedMarbleWall,

            // boulders and custom env decorations
            charoitBoulder, eboniteBoulder, cliveliteBoulder, mossyCliveliteBoulder, ectorumSandBoulder,
            bluishMossShoots, bluishMossBush, bluishMossTree,
            sulfurLayering, largeSulfurLayering, pyriteCluster,
            giantThoriumCrystal,
            kyanicCluster, giantKyanicCluster,

            // colored tiles

            // ores
            oreBismuth, oreZinc, oreLithium, oreEctorumThorium, oreBorum,

            // wall ores
            wallOreBismuth, wallOreZinc, wallOreLithium, thoriumCrystalWall, kyaniteWall,

            // crafting
            highPressureSiliconSmelter, compoundAssembler, sulfideCrucible,

            // sandbox
            pressureSource, pressureVoid,

            // walls
            bismuthWall, bismuthWallLarge, darkScrapWall, darkScrapWallLarge, pressurizedWall, pressurizedWallLarge, powersteelWall /*TODO: литий + электросталь*/, powersteelWallLarge, radioactiveWall /*TODO: подумать*/, radioactiveWallLarge,
            zincCasing, zincCasingLarge, chromiumCasing, chromiumCasingLarge, reflectiveCasing, reflectiveCasingLarge,

            // defense utils (regen and etc)
            radarDevice,

            // transport
            pneumaticDuct, armoredPneumaticDuct, pneumaticDuctRouter, pneumaticDuctBridge, pneumaticOverflowDuct, pneumaticUnderflowDuct,

            // liquid distribution etc.
            pneumaticPump,  compositeConduit, armoredCompositeConduit, compositeLiquidRouter, compositeLiquidJunction, compositeBridgeConduit, compositeLiquidContainer, compositeLiquidTank,

            // power
            geyserTurbine,
            powerTransmitter,
            zincBattery, zincBatteryLarge,

            // production (drills and bores)
            impulseBore, largeImpulseBore, highPrecisionBore, rotorDrill, vacuumDrill, magneticDrill,
            cliffShredder, crusher,

            // cores, imitators and storages
            coreSpark, coreSunrise,
            coreSparkImitator, coreSunriseImitator,

            // turrets
            sentinel,
            brazier,

            // unit factories
            mechAssemblyUnit, spiderAssemblyUnit, airAssemblyUnit, navalAssemblyUnit,
            groundReassemblyUnit

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
            variants = 7;
        }};

        smoothCharoit = new Floor("smooth-charoit") {{
            variants = 3;

            blendGroup = charoit;
        }};

        charoitCrater = new Floor("charoit-crater") {{
            variants = 3;

            blendGroup = charoit;
        }};

        charoitCraterBig = new LargeCrater("charoit-crater-big") {{
            variants = 3;

            parent = blendGroup = charoit;
        }};

        ebonite = new Floor("ebonite") {{
            variants = 6;
        }};

        smoothEbonite = new Floor("smooth-ebonite") {{
            variants = 4;

            blendGroup = ebonite;
        }};

        smoothClivelite = new Floor("smooth-clivelite") {{
            variants = 3;
        }};

        clivelite = new Floor("clivelite") {{
            variants = 3;

            blendGroup = smoothClivelite;
        }};

        crushedClivelite = new Floor("crushed-clivelite") {{
            variants = 7;

            blendGroup = smoothClivelite;
        }};

        bluishMoss = new Floor("bluish-moss", 4);

        ectorumSand = new Floor("ectorum-sand", 4) {{
            itemDrop = Items.sand;

            playerUnmineable = true;
        }};

        ectorumQuicksand = new EctorumQuicksand("ectorum-quicksand") {{
            speedMultiplier = 0.35f;
            dragMultiplier = 2.25f;

            status = EctoStatusEffects.sanded; // В песке
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
            itemDrop = Items.sand;

            playerUnmineable = true;

            blendGroup = ectorumSand;
        }};

        boricSandWater = new ParticledShallowLiquid("boric-sand-water") {{

            speedMultiplier = 0.7f;
            statusDuration = 70f;
            supportsOverlay = true;

            liquidMultiplier = 0.8f;

            albedo = 0.9f;

            blendGroup = ectorumSand;
        }};

        boricWater = new Floor("boric-water") {{
            variants = 0;

            speedMultiplier = 0.5f;
            status = StatusEffects.wet;
            statusDuration = 90f;
            liquidDrop = Liquids.water;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        deepBoricWater = new Floor("deep-boric-water") {{
            variants = 0;

            speedMultiplier = 0.4f;
            drownTime = 6f * 60f;
            status = StatusEffects.wet;
            statusDuration = 90f;
            liquidDrop = Liquids.water;
            liquidMultiplier = 1.2f;

            isLiquid = true;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        sulfurFloor = new Floor("sulfur-floor", 6) {{

            itemDrop = EctoItems.sulfur;
            playerUnmineable = true;
        }};

        smoothSulfurFloor = new Floor("smooth-sulfur-floor") {{
            itemDrop = EctoItems.sulfur;

            playerUnmineable = true;

            blendGroup = sulfurFloor;
        }};

        sulfurCrater = new Floor("sulfur-crater") {{

            blendGroup = sulfurFloor;
        }};

        smoothSulfurFloorSubmerged = new ParticledShallowLiquid("smooth-sulfur-floor-submerged") {{

            speedMultiplier = 0.6f;
            dragMultiplier = 1.1f;
            statusDuration = 50f;
            supportsOverlay = true;

            albedo = 0.9f;
        }};

        smoothSulfurFloorWater = new ParticledShallowLiquid("smooth-sulfur-floor-water") {{

            speedMultiplier = 0.7f;
            statusDuration = 50f;
            supportsOverlay = true;

            albedo = 0.9f;
        }};

        sulfurSolution = new ParticledFloor("sulfur-solution-tile") {{
            variants = 0;
            drownTime = (7f * 60f);

            status = StatusEffects.corroded;
            statusDuration = 240f;
            speedMultiplier = 0.4f;
            dragMultiplier = 5f;

            liquidDrop = EctoLiquids.sulfurSolution;
            isLiquid = true;

            cacheLayer = EctoShaders.sulfurSolution;

            updateEffect = Fx.vaporSmall;
        }};

        boricIce = new Floor("boric-ice", 6);

        crackedMarble = new Floor("cracked-marble", 5);

        smoothMarble = new Floor("smooth-marble") {{
            blendGroup = crackedMarble;
        }};

        crackedThoriumite = new Floor("cracked-thoriumite-floor", 5);

        thoriumite = new Floor("thoriumite-floor", 4) {{
            blendGroup = crackedThoriumite;
        }};

        smoothThoriumite = new Floor("smooth-thoriumite-floor", 4) {{
            blendGroup = crackedThoriumite;
        }};

        kyanicStone = new Floor("kyanic-stone");

        kyanicStoneNugget = new Floor("kyanic-stone-nugget") {{
            blendGroup = kyanicStone;
        }};

        ((ParticledShallowLiquid)boricSandWater).set(Blocks.water, EctoBlocks.ectorumSand);
        ((ParticledShallowLiquid)smoothSulfurFloorWater).set(Blocks.water, EctoBlocks.smoothSulfurFloor);
        ((ParticledShallowLiquid)smoothSulfurFloorSubmerged).set(EctoBlocks.sulfurSolution, EctoBlocks.smoothSulfurFloor);

        // handmade environment

        polishedMarble = new Floor("polished-marble") {
            {
                autotile = true;
                drawEdgeOut = false;
                drawEdgeIn = false;

                autotileMidVariants = 3;
            }

            @Override
            public void load() {
                super.load();
                autotileRegions = RegionSlicer.splitRegion(Core.atlas.find(name + "-autotile"), 32, 32, 0);
            }
        };

        metalPlates1 = new Floor("metal-plates-1") {
            {
                autotile = true;
                drawEdgeOut = false;
                drawEdgeIn = false;
                buildVisibility = BuildVisibility.hidden;
            }

            @Override
            public void load() {
                super.load();
                autotileRegions = RegionSlicer.splitRegion(Core.atlas.find(name + "-autotile"), 32, 32, 0);
            }
        };

        // geysers

        charoitGeyser = new SteamGeyser("charoit-geyser") {{
            parent = blendGroup = charoit;

            activeTime = 10f * 60f;
            passiveTime = 16f * 60f;

            activeEfficiency = 0.8f;
            passiveEfficiency = 0.4f;
            unitDamageTaken = 1.58f;
        }};

        eboniteGeyser = new SteamGeyser("ebonite-geyser") {{
            parent = blendGroup = ebonite;

            activeTime = 7f * 60f;
            passiveTime = 28f * 60f;

            activeEfficiency = 1.05f;
            passiveEfficiency = 0.1f;
            unitDamageTaken = 1.58f;
        }};

        cliveliteGeyser = new SteamGeyser("clivelite-geyser") {{
            parent = blendGroup = clivelite;
            variants = 3;
            activeTime = 10f * 60f;
            passiveTime = 24f * 60f;

            activeEfficiency = 1f;
            passiveEfficiency = 0.16f;
            unitDamageTaken = 1.58f;
        }};

        sulfurGeyser = new SteamGeyser("sulfur-geyser") {{
            parent = blendGroup = sulfurFloor;

            activeTime = 8f * 60f;
            passiveTime = 30f * 60f;

            activeEfficiency = 2.95f;
            passiveEfficiency = 0.8f;
            unitDamageTaken = 1.58f;
        }};

        // envWalls

        malachiteWall = new StaticWall("malachite-wall") {{
            variants = 3;

            malachite.asFloor().wall = this;
        }};

        charoitWall = new StaticWall("charoit-wall") {{
            variants = 3;

            charoit.asFloor().wall = this;
        }};

        eboniteWall = new StaticWall("ebonite-wall") {{
            variants = 3;

            ebonite.asFloor().wall = this;
            attributes.set(Attribute.sand, 0.1f);
        }};

        cliveliteWall = new StaticWall("clivelite-wall") {{
            variants = 3;

            clivelite.asFloor().wall = smoothClivelite.asFloor().wall = crushedClivelite.asFloor().wall = this;
            attributes.set(Attribute.sand, 0.65f);
        }};

        mossyCliveliteWall = new StaticWall("mossy-clivelite-wall") {{
            variants = 3;
        }};

        bluishMossWall = new StaticWall("bluish-moss-wall") {{
            variants = 3;

            bluishMoss.asFloor().wall = this;
        }};

        ectorumSandstoneWall = new StaticWall("ectorum-sandstone-wall") {{
            variants = 3;

            ectorumSand.asFloor().wall = ectorumQuicksand.asFloor().wall = ectorumSandstone.asFloor().wall = this;
            attributes.set(Attribute.sand, 1f);
        }};

        sulfurWall = new StaticWall("sulfur-wall") {{
            variants = 3;

            sulfurFloor.asFloor().wall = smoothSulfurFloor.asFloor().wall = sulfurCrater.asFloor().wall = this;
            attributes.set(EctoAttributes.sulfur, 0.8f);
        }};

        pyritedSulfurWall = new StaticWall("pyrited-sulfur-wall") {{
            variants = 3;

            //sulfurFloor.asFloor().wall = smoothSulfurFloor.asFloor().wall = sulfurCrater.asFloor().wall = this;
            attributes.set(EctoAttributes.sulfur, 0.45f);
        }};

        boricIceWall = new  StaticWall("boric-ice-wall") {{
            variants = 3;

            boricIce.asFloor().wall = this;
        }};

        thoriumiteWall = new StaticWall("thoriumite-wall") {{
            variants = 3;

            thoriumite.asFloor().wall = smoothThoriumite.asFloor().wall = this;
        }};

        kyanicStoneWall = new StaticWall("kyanic-stone-wall") {{
            variants = 3;

            kyanicStone.asFloor().wall = kyanicStoneNugget.asFloor().wall = this;
        }};

        polishedMarbleWall = new StaticWall("polished-marble-wall") {{
            variants = 3;

            polishedMarble.asFloor().wall = this;
        }};


        // boulders etc. decorations
        charoitBoulder = new StaticProp("charoit-boulder") {{
            variants = 3;

            charoit.asFloor().decoration = this;
        }};

        eboniteBoulder = new StaticProp("ebonite-boulder") {{
            variants = 3;

            ebonite.asFloor().decoration = this;
        }};

        cliveliteBoulder = new StaticProp("clivelite-boulder") {{
            variants = 3;

            clivelite.asFloor().decoration = smoothClivelite.asFloor().decoration = crushedClivelite.asFloor().decoration = this;
        }};

        mossyCliveliteBoulder = new StaticProp("mossy-clivelite-boulder") {{
            variants = 3;

            bluishMoss.asFloor().decoration = this;
        }};

        ectorumSandBoulder = new StaticProp("ectorum-sand-boulder") {{
            variants = 3;

            ectorumSand.asFloor().decoration = ectorumSandstone.asFloor().decoration = this;
        }};

        bluishMossShoots = new OverlayFloor("bluish-moss-shoots") {{
            variants = 4;
        }};

        bluishMossBush = new Prop("bluish-moss-bush") {{
            variants = 3;

            bluishMoss.asFloor().decoration = this;
            breakSound = Sounds.plantBreak;
            //obstructsLight = false;
        }};

        bluishMossTree = new TreeBlock("bluish-moss-tree") {{
            variants = 3;
        }};

        sulfurLayering = new TallBlock("sulfur-layering") {{ // Серное наслоение
            variants = 3;
            clipSize = 128f;
            shadowAlpha = 0.5f;
            shadowOffset = -2.5f;
        }};

        largeSulfurLayering = new TallBlock("sulfur-layering-large") {{
            variants = 3;
            clipSize = 128f;
            shadowAlpha = 0.5f;
            shadowOffset = -2.5f;
        }};

        pyriteCluster = new TallBlock("pyrite-cluster") {{
            variants = 3;
            clipSize = 128f;
            shadowAlpha = 0.5f;
            shadowOffset = -2.5f;
        }};

        giantThoriumCrystal = new TallBlock("thorium-crystal-giant") {
            {
                variants = 0;
                clipSize = 128f;
            }

            @Override
            public TextureRegion[] icons() {
                TextureRegion full = Core.atlas.find(name + "-full");
                if (full.found()) return new TextureRegion[]{full};
                return super.icons();
            }
        };

        kyanicCluster = new TallBlock("kyanic-cluster") {{
            variants = 3;
            clipSize = 128f;
        }};

        giantKyanicCluster = new TallBlock("kyanic-cluster-giant") {{
            variants = 0;
            clipSize = 128f;
        }};

        // colored tiles

        // ore
        oreBismuth = new OreBlock("ore-bismuth", EctoItems.bismuth);

        oreZinc = new OreBlock("ore-zinc", EctoItems.zinc);

        oreLithium = new OreBlock("ore-lithium", EctoItems.lithium);

        oreEctorumThorium = new OreBlock("ore-ectorum-thorium", Items.thorium);

        // wall ore

        wallOreBismuth = new OreBlock("wall-ore-bismuth", EctoItems.bismuth) {{
            wallOre = true;
        }};

        wallOreZinc = new OreBlock("wall-ore-zinc", EctoItems.zinc) {{
            wallOre = true;
        }};

        wallOreLithium = new OreBlock("wall-ore-lithium", EctoItems.lithium) {{
            wallOre = true;
        }};

        thoriumCrystalWall = new StaticWall("thorium-crystal-wall") {{
            itemDrop = Items.thorium;
            variants = 3;
        }};

        kyaniteWall = new StaticWall("kyanite-wall") {{
            itemDrop = EctoItems.kyanite;
            variants = 3;
        }};

        // crafting

        highPressureSiliconSmelter = new PressurizedCrafter("high-pressure-silicon-smelter") {{
            requirements(Category.crafting, with(EctoItems.bismuth, 45, Items.graphite, 30, EctoItems.zinc, 10));
            craftEffect = Fx.none;
            outputItem = new ItemStack(Items.silicon, 2);
            craftTime = 70f;
            size = 3;
            hasPower = true;
            hasLiquids = false;
            envEnabled |= Env.space | Env.underwater;
            envDisabled = Env.none;
            itemCapacity = 30;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(), new DrawDefault());
            fogRadius = 3;
            researchCost = with(EctoItems.bismuth, 400, Items.graphite, 250, EctoItems.zinc, 80);
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
            consumePower(120f / 60f);
        }};

        compoundAssembler = new GenericCrafter("compound-assembler") {{
            requirements(Category.crafting, with(EctoItems.bismuth, 30, Items.graphite, 10, Items.silicon, 45));
            craftEffect = Fx.none;
            outputItem = new ItemStack(EctoItems.hydrodefensiveCompound, 1);
            craftTime = 160f;
            size = fogRadius = 3;
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
                        flameColor = Color.valueOf("918279");
                    }},
                    new DrawDefault(),
                    new DrawGlowRegion() {{
                        alpha = 0.8f;
                        color = Color.valueOf("7a9fb5");
                        glowIntensity = 0.3f;
                        glowScale = 0f;
                    }}
            );

            researchCost = with(EctoItems.bismuth, 600, Items.graphite, 120, Items.silicon, 450);

            ambientSound = Sounds.loopMachine2;
            ambientSoundVolume = 0.12f;

            consumeItems(with(EctoItems.bismuth, 3, Items.silicon, 1));
            consumePower(90f / 60f);
        }};

        sulfideCrucible = new BalancedCrafter("sulfide-crucible") {{
            requirements(Category.crafting, with(EctoItems.bismuth, 110, Items.graphite, 70, Items.silicon, 130));
            outputItem = new ItemStack(EctoItems.sulfide, 1);

            size = fogRadius = 3;
            hasPower = true;
            itemCapacity = 25;

            aspects.add(new HeatAspect(120f, 0.35f),
                    new PressureAspect(240f, 0.5f)
            );

            researchCostMultiplier = 15f;

            consumeItems(with(EctoItems.sulfur, 3, EctoItems.zinc, 5));
            basePowerDraw = 1250f / 60f;
        }};

        int wallHealthMultiplier = 4;

        bismuthWall = new Wall("bismuth-wall") {{
            requirements(Category.defense, with(EctoItems.bismuth, 6));
            health = 115 * wallHealthMultiplier;
            armor = 3f;
            buildCostMultiplier = 12f;
        }};

        bismuthWallLarge = new Wall("bismuth-wall-large") {{
            requirements(Category.defense, ItemStack.mult(bismuthWall.requirements, 4));
            health = bismuthWall.health * 4;
            armor = bismuthWall.armor;
            buildCostMultiplier = 10f;
            size = 2;
        }};

        darkScrapWall = new Wall("dark-scrap-wall") {{
            requirements(Category.defense, with(EctoItems.bismuth, 2, Items.scrap, 6));
            health = 90 * wallHealthMultiplier;
            variants = 4;

            armor = 1f;
            buildCostMultiplier = 8f;
            size = 1;
        }};

        darkScrapWallLarge = new Wall("dark-scrap-wall-large") {{
            requirements(Category.defense, ItemStack.mult(darkScrapWall.requirements, 4));
            health = darkScrapWall.health * 4;
            variants = 4;

            armor = darkScrapWall.armor;
            buildCostMultiplier = 6f;
            size = 2;
        }};

        zincCasing = new SelfRegenWallCasing("zinc-casing") {{
            requirements(Category.defense,  with(EctoItems.zinc, 6, Items.silicon, 4));
            health = 50;
            armor = 2f;
            buildCostMultiplier = 8f;
            size = 1;

            damageAbsorption = 0.4f;

            healPercent = 2f / 60f;
        }};

        zincCasingLarge = new SelfRegenWallCasing("zinc-casing-large") {{
            requirements(Category.defense,  ItemStack.mult(zincCasing.requirements, 4));
            health = zincCasing.health * 4;
            armor = zincCasing.armor;
            buildCostMultiplier = 6f;
            size = 2;

            damageAbsorption = 0.4f;

            healPercent = 2f / 60f;
        }};

        radarDevice = new UnitSignatureRadar("radar-device") {{
            requirements(Category.effect, BuildVisibility.fogOnly, with(EctoItems.bismuth, 30, Items.graphite, 45));
            health = 60;
            outlineColor = Color.valueOf("4a4b53");
            fogRadius = 20;
            extraRadius = 35;

            signatureCycle = 1.2f * 60f;
            signatureStroke = 6f;
            signatureSizeScl = 2f;

            boostedRadiusScl = 1.3f;

            researchCostMultiplier = 0.05f;

            consumePower(0.6f);
            consumeItem(Items.silicon).boost();
        }};

        pneumaticDuct = new PneumaticDuct("pneumatic-duct") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 1));
            health = 80;
            speed = 6f;
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
            health = 250;
            speed = 6f;
            armored = true;
            researchCost = with(EctoItems.bismuth, 150, EctoItems.chromium, 70);

            operatingPressure = 1f;
            thresholdPressure = 2.8f;

            isPressureRequired = false;

            minEfficiencyCoeff = 0.3f;
            maxEfficiencyCoeff = 1.33f;
            outflowTanhFactor = 11.5f;
            outflowExponentCoefficient = 2.8f;
            criticalPressure = 12f;
            superCriticalPressure = 18f;
        }};

        pneumaticDuctRouter = new PneumaticDuctRouter("pneumatic-duct-router") {{
            requirements(Category.distribution, with(EctoItems.bismuth, 4));
            health = 60;
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
            requirements(Category.distribution, with(EctoItems.bismuth, 8, Items.graphite, 2));
            health = 60;
            speed = 3f;
            buildCostMultiplier = 2f;
            researchCost = with(EctoItems.bismuth, 50, Items.graphite, 25);

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
            requirements(Category.distribution, with(EctoItems.bismuth, 2, Items.graphite, 1));
            health = 60;
            speed = 3f;
            researchCost = with(EctoItems.bismuth, 100, Items.graphite, 80);

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
            requirements(Category.distribution, ItemStack.copy(pneumaticOverflowDuct.requirements));
            health = 60;
            speed = 3f;
            invert = true;
            researchCost = ItemStack.copy(pneumaticOverflowDuct.researchCost);

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

        // Liquid distribution

        pneumaticPump = new PressurizedPump("pneumatic-pump") {{
            requirements(Category.liquid, with(EctoItems.bismuth, 70, EctoItems.hydrodefensiveCompound, 40, Items.silicon, 25));
            squareSprite = false;

            pumpAmount = 0.25f;
            liquidCapacity = 110f;
            hasPower = true;

            researchCost = with(EctoItems.bismuth, 700, Items.silicon, 225, EctoItems.hydrodefensiveCompound, 25);
            consumePower(50f / 60f);
            size = 2;
        }};

        compositeConduit = new IntegrityConduit("composite-conduit") {{
            requirements(Category.liquid, ItemStack.with(EctoItems.hydrodefensiveCompound, 1));
            health = 180;
            liquidCapacity = 30f;

            leaks = true;
            underBullets = true;

            leakThreshold = 0.8f;
            leakFraction = 0.2f;

            leakDamages = true;
            leakDamageThreshold = 0.1f;
            leakDamagePerUnit = 0.02f;

            researchCostMultiplier = 3f;
            researchCost = with(EctoItems.hydrodefensiveCompound, 10);

            explosivenessScale = flammabilityScale = 12f/50f;
        }};

        armoredCompositeConduit = new ArmoredConduit("armored-composite-conduit") {{
            requirements(Category.liquid, ItemStack.with(EctoItems.hydrodefensiveCompound, 2, EctoItems.chromium, 1));
            health = 320;
            liquidCapacity = 50f;

            leaks = false;
            underBullets = true;

            researchCostMultiplier = 3f;

            explosivenessScale = flammabilityScale = 18f/50f;

        }};

        compositeLiquidRouter = new IntegrityLiquidRouter("composite-liquid-router") {{
            requirements(Category.liquid, ItemStack.with(EctoItems.hydrodefensiveCompound, 4, Items.graphite, 1));
            health = 200;

            researchCostMultiplier = 0.1f;

            liquidCapacity = 150f;
            liquidPadding = 3f/4f;

            leakThreshold = 0.75f;
            leakFraction = 0.2f;

            leakDamages = true;
            leakDamageThreshold = 0.1f;
            leakDamagePerUnit = 0.02f;

            underBullets = true;
            solid = false;
            squareSprite = false;

            explosivenessScale = flammabilityScale = 14f/50f;
        }};

        compositeLiquidJunction = new IntegrityLiquidJunction("composite-liquid-junction") {{
            requirements(Category.liquid, ItemStack.with(EctoItems.hydrodefensiveCompound, 6, Items.graphite, 2));
            buildCostMultiplier = 3f;

            health = 180;

            ((Conduit)compositeConduit).junctionReplacement = ((Conduit)armoredCompositeConduit).junctionReplacement = this;
            researchCostMultiplier = 0.1f;

            leakThreshold = 0.8f;
            leakFraction = 0.2f;

            leakDamages = true;
            leakDamageThreshold = 0.1f;
            leakDamagePerUnit = 0.02f;

            solid = false;
            underBullets = true;
            squareSprite = false;
        }};

        compositeBridgeConduit = new IntegrityLiquidBridge("composite-bridge-conduit") {{
            requirements(Category.liquid, ItemStack.with(EctoItems.hydrodefensiveCompound, 6, Items.graphite, 2));
            range = 4;

            hasPower = false;

            liquidCapacity = 120f;
            researchCostMultiplier = 0.5f;
            underBullets = true;
            health = 220;

            explosivenessScale = flammabilityScale = 16f/120f;
            floating = true;
            squareSprite = false;

            leakThreshold = 0.65f;
            leakFraction = 0.15f;

            leakDamages = true;
            leakDamageThreshold = 0.1f;
            leakDamagePerUnit = 0.02f;

            ((Conduit)compositeConduit).rotBridgeReplacement = ((Conduit)armoredCompositeConduit).junctionReplacement = this;
        }};

        compositeLiquidContainer = new IntegrityLiquidRouter("composite-liquid-container") {{
            requirements(Category.liquid, ItemStack.with(EctoItems.hydrodefensiveCompound, 20, Items.graphite, 15, EctoItems.chromium, 5));
            size = 2;
            health = 380;
            liquidPadding = 6f/4f;
            researchCostMultiplier = 2;
            solid = true;
            squareSprite = false;

            leakThreshold = 0.7f;
            leakFraction = 0.15f;
            liquidCapacity = 850f;

            leakDamages = true;
            leakDamageThreshold = 0.12f;
            leakDamagePerUnit = 0.04f;

            explosivenessScale = flammabilityScale = 25f/150f;
        }};

        // Power

        geyserTurbine = new GeyserGenerator("geyser-turbine") {{
            requirements(Category.power, with(EctoItems.bismuth, 40, Items.graphite, 15));
            size = 3;
            attribute = EctoAttributes.geyser;
            powerProduction = 100f / 60f;

            researchCost = with(EctoItems.bismuth, 25, Items.graphite, 15);

            productionChangeSpeed = 0.007f;

            displayEfficiency = false;
            generateEffect = Fx.ventSteam;
            effectChance = 0.009f;

            ambientSound = Sounds.loopHum;
            ambientSoundVolume = 0.06f;

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawBlurSpin("-rotator", 11f) {{
                        blurThresh = 0.54f;
                    }},
                    new DrawDefault()
            );

            fogRadius = size;
        }};

        powerTransmitter = new OctoBeamNode("power-transmitter") {{
            requirements(Category.power, with(EctoItems.bismuth, 4, Items.graphite, 1));
            consumesPower = outputsPower = true;
            health = 90;
            range = 8;
            fogRadius = 1;
            researchCost = with(EctoItems.bismuth, 10, Items.graphite, 5);
            crushFragile = true;
        }};

        zincBattery = new Battery("zinc-battery") {{
            requirements(Category.power, with(EctoItems.zinc, 15, Items.graphite, 10));
            health = 90;
            consumePowerBuffered(800f);
            researchCostMultiplier = 0.25f;

            baseExplosiveness = 1f;
            breakSound = Sounds.blockExplodeElectric;
        }};

        zincBatteryLarge = new Battery("zinc-battery-large") {{
            requirements(Category.power, with(EctoItems.zinc, 30, Items.graphite, 25, Items.silicon, 10));
            health = 250;
            size = 2;
            consumePowerBuffered(5500f);

            baseExplosiveness = 3f;
            breakSound = Sounds.blockExplodeElectricBig;
        }};

        impulseBore = new BurstBeamDrill("impulse-bore") {{
            requirements(Category.production, with(EctoItems.bismuth, 25, Items.graphite, 10));
            consumePower(20 / 60f);

            drillTime = 10f * 60f;
            optionalBoostIntensity = 1.3f;

            tier = 3;
            size = 2;
            range = 5;
            fogRadius = size;
            itemCapacity = 40;
            blockedItem = EctoItems.lithium;

            burstMultiplier = 5;

            researchCost = with(EctoItems.bismuth, 25);

            consumeLiquid(Liquids.water, 15f / 60f).boost();
        }};

        cliffShredder = new MultipleWallCrafter("cliff-shredder") {{
            requirements(Category.production, with(EctoItems.bismuth, 20, EctoItems.zinc, 8));
            consumePower(15 / 60f);

            drillTime = 120f;
            size = 2;
            health = 90;
            fogRadius = size;
            ambientSound = Sounds.loopDrill;
            ambientSoundVolume = 0.04f;

            wallAttributes.add(
                    new AttributeWall(Attribute.sand, Items.sand),
                    new AttributeWall(EctoAttributes.sulfur, EctoItems.sulfur),
                    new AttributeWall(EctoAttributes.teynorite, EctoItems.teynorite)
            );
        }};

        coreSpark = new CoreBlock("core-spark") {{
            requirements(Category.effect, with(EctoItems.bismuth, 1800, EctoItems.zinc, 1000, Items.graphite, 1200));
            alwaysUnlocked = true;

            isFirstTier = true;
            unitType = EctoUnitTypes.glare;
            health = 2200;
            itemCapacity = 3800;
            size = 3;

            squareSprite = false;

            unitCapModifier = 10;
            buildCostMultiplier = 0.8f;
        }};

        coreSunrise = new CoreBlock("core-sunrise") {{
            requirements(Category.effect, with(EctoItems.bismuth, 7200, Items.silicon, 6000, EctoItems.chromium, 6000, EctoItems.sulfide, 600));

            health = 4300;
            itemCapacity = 7500;
            size = 4;

            squareSprite = false;

            unitCapModifier = 14;
            buildCostMultiplier = 1.1f;
            researchCostMultiplier = 1.3f;
        }};

        coreSparkImitator = new CoreBlockImitator("core-spark-imitator", (CoreBlock) coreSpark) {{
            requirements(Category.effect, with(EctoItems.bismuth, 900, EctoItems.zinc, 450, Items.graphite, 450));

            health = 1200;
            itemCapacity = 500;

        }};

        sentinel = new AmmoTargetItemTurret("sentinel") {{
            requirements(Category.turret, with(EctoItems.bismuth, 120, Items.graphite, 100));
            Effect sfe = EctoFx.shootTurretBigColor;

            ammo(
                    EctoItems.bismuth, new BasicBulletType(16, 35) {{
                        width = 14f;
                        height = 15f;
                        lifetime = 10;
                        hitSize = 10f;
                        shootEffect = sfe;
                        smokeEffect = Fx.none;
                        ammoMultiplier = 1;

                        pierce = false;
                        pierceBuilding = false;

                        collidesAir = false;

                        buildingDamageMultiplier = 0.3f;

                        hitColor = backColor = trailColor = Pal.darkMetal;
                        frontColor = EctoPal.darkGreenShot;
                        trailWidth = 2.1f;
                        trailLength = 5;
                        hitEffect = Fx.blastExplosion;
                        despawnEffect = Fx.hitBulletColor;

                        splashDamageRadius = 1.2f * 8f;
                        splashDamage = 55f;
                        scaledSplashDamage = true;

                        knockback = 0.6f;
                    }},

                    Items.graphite, new BasicBulletType(16.6666666f, 55) {{
                        width = 16f;
                        height = 17f;
                        lifetime = 12;

                        hitSize = 8f;
                        shootEffect = sfe;
                        smokeEffect = Fx.none;
                        ammoMultiplier = 2;
                        rangeChange = 5f * 8f;

                        collidesGround = false;

                        hitColor = backColor = trailColor = Pal.graphiteAmmoBack;
                        frontColor = Pal.graphiteAmmoFront;
                        trailWidth = 1.4f;
                        trailLength = 4;
                        hitEffect = despawnEffect = Fx.hitBulletColor;

                        fragBullets = 4;
                        fragSpread = 30f;
                        fragRandomSpread = 3f;
                        fragVelocityMin = 1f;

                        knockback = 0.4f;

                        fragBullet = new BasicBulletType(6.4f, 15) {{
                            lifetime = 11f;

                            width = 6f;
                            height = 8f;

                            hitSize = 5f;

                            targetGround = false;
                            homingPower = 0.2f;

                            hitColor = backColor = trailColor = Pal.graphiteAmmoBack;
                            frontColor = Pal.graphiteAmmoFront;
                            trailWidth = 0.9f;
                            trailLength = 2;
                            hitEffect = despawnEffect = Fx.hitBulletColor;
                        }};
                    }}
            );

            shootSound = Sounds.shootScepter;

            targetUnderBlocks = false;
            shake = 2.85f;
            ammoPerShot = 2;
            drawer = new DrawTurret("ectorum-") {{
                parts.add(new RegionPart("-barrel") {{
                    progress = PartProgress.recoil.curve(Interp.pow2Out);
                    moveY = -3.2f;
                    under = true;
                    recoilTime = 40f;
                    heatColor = new Color(Pal.turretHeat).a(0.6f);
                }});
            }};

            cooldownTime = 35;

            shootY = 7f;
            outlineColor = Color.valueOf("1a2317");

            size = 2;
            itemCapacity = 15;
            envEnabled |= Env.space;
            reload = 50f;
            recoil = 0.3f;
            range = 20f * 8f;
            shootCone = 3f;
            scaledHealth = 150;
            rotateSpeed = 1.5f;
            researchCost = with(EctoItems.bismuth, 450, Items.graphite, 300);

            coolantMultiplier = 2f;
            coolant = consume(new ConsumeLiquid(Liquids.water, 15f / 60f));
            limitRange(12f);
        }};

        brazier = new ItemTurret("brazier") {{
            requirements(Category.turret, with(EctoItems.bismuth, 300, Items.silicon, 150, Items.graphite, 80));

            drawer = new DrawTurret("ectorum-"){{
                parts.addAll(
                        new RegionPart("-barrel"){{
                            progress = PartProgress.recoil.curve(Interp.pow2Out);
                            moveY = -1.2f;
                            under = true;
                            recoilTime = 8f;
                        }},
                        new RegionPart("-overlay"){{
                            drawRegion = false;
                            heatColor = Color.valueOf("ffaf54").a(0.6f);
                            heatProgress = PartProgress.warmup;
                        }},
                        new RegionPart("-flap") {{
                            progress = PartProgress.recoil.blend(PartProgress.warmup.curve(Interp.pow2), 0.35f);
                            moveY = -3.2f;
                            recoilTime = 8f;
                            heatColor = new Color(Pal.turretHeat).a(0.6f);
                        }},

                        new RegionPart("-top"),

                        new RegionPart("-blade") {{
                            heatProgress = PartProgress.warmup;
                            heatColor = Color.sky.cpy().a(0.9f);
                            mirror = true;
                            under = true;
                            moveY = -1f;
                            moveX = 0.15f;
                            moveRot = -8;
                        }}
                );
            }};

            ammo(
                    EctoItems.zinc, new BasicBulletType(10f, 70, "circle-bullet") {{
                        width = height = 11;
                        shrinkY = 0;
                        velocityRnd = 0.11f;
                        collidesGround = collidesAir = true;
                        collidesTiles = false;
                        shootEffect = Fx.shootBig2;
                        smokeEffect = Fx.shootSmokeDisperse;
                        frontColor = Color.valueOf("ffe6c9");
                        backColor = trailColor = hitColor = Color.valueOf("f7a677");
                        ammoMultiplier = 3f;
                        trailEffect = EctoFx.trailBulletSparks;
                        trailChance = 0.6f;
                        lifetime = 30f;
                        trailRotation = true;

                        status = EctoStatusEffects.zinced;
                        statusDuration = 2.8f * 60f;

                        hitEffect = despawnEffect = new MultiEffect(Fx.hitBulletColor, EctoFx.bulletDespawnSparks);

                        weaveScale = 6;
                        weaveMag = 0.9f;
                    }}
            );
            range = 35f * 8f;
            rotateSpeed = 3.2f;

            reload = 15;
            size = 3;
            scaledHealth = 130;
            inaccuracy = 4;
            outlineColor = Color.valueOf("2f2b2d");
            shootWarmupSpeed = 0.04f;
            minWarmup = 0.98f;
            squareSprite = false;

            consumePower(120f / 60f);
        }};

        mechAssemblyUnit = new UnitFactory("mech-assembly-unit") {{
            requirements(Category.units, with(EctoItems.bismuth, 200, Items.silicon, 130));

            size = 3;
            configurable = true;
            plans.add(new UnitPlan(EctoUnitTypes.fist, 35f * 60f, with(EctoItems.bismuth, 30, Items.silicon, 45)));
            regionSuffix = "-awake";
            fogRadius = size;
            researchCost = with(EctoItems.bismuth, 600, Items.silicon, 150);
            consumePower(90f / 60f);
        }};

        spiderAssemblyUnit = new UnitFactory("spider-assembly-unit") {{
            requirements(Category.units, with(EctoItems.bismuth, 110, Items.silicon, 180, EctoItems.zinc, 100));

            size = 3;
            configurable = true;
            plans.add(new UnitPlan(EctoUnitTypes.echo, 30f * 60f, with(EctoItems.bismuth, 30, Items.silicon, 65, EctoItems.zinc, 45)));
            regionSuffix = "-awake";
            fogRadius = size;
            researchCostMultiplier = 1.2f;
            consumePower(90f / 60f);
        }};

        airAssemblyUnit = new UnitFactory("air-assembly-unit") {{
            requirements(Category.units, with(EctoItems.bismuth, 110, Items.silicon, 180, Items.graphite, 80));

            size = 3;
            configurable = true;
            plans.add(new UnitPlan(EctoUnitTypes.discharge, 35f * 60f, with(EctoItems.bismuth, 20, Items.silicon, 65, Items.graphite, 70)));
            regionSuffix = "-awake";
            fogRadius = size;
            researchCostMultiplier = 1.4f;
            consumePower(90f / 60f);
        }};

        groundReassemblyUnit = new BranchReconstructor("ground-reassembly-unit") {{
            requirements(Category.units, with(EctoItems.bismuth, 120, Items.silicon, 100, EctoItems.chromium, 60));

            size = 3;
            configurable = true;
            plans.addAll(
                    new UpgradePlan(EctoUnitTypes.fist, EctoUnitTypes.sickle, 40f * 60f),
                    new UpgradePlan(EctoUnitTypes.echo, EctoUnitTypes.whisper, 50f * 60f)
            );
            regionSuffix = "-awake";
            fogRadius = size;
            researchCostMultiplier = 1.4f;

            defaultRequirements = with(EctoItems.bismuth, 80, Items.silicon, 110, EctoItems.chromium, 60);
            constructTime = 60f * 60f;
            consumePower(250f / 60f);
        }};
    }
}