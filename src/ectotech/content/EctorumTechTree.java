package ectotech.content;

import arc.struct.Seq;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.game.Objectives.*;

import static ectotech.content.EctoBlocks.*;
import static ectotech.content.EctoSectorPresets.*;
import static mindustry.content.TechTree.*;


public class EctorumTechTree {
    public static void load() {
        Seq<Objective> ectorumSector = Seq.with(new OnPlanet(EctoPlanets.ectorum));

        EctoPlanets.ectorum.techTree = nodeRoot("ectorum", coreSpark, true, () -> {

            // Item distribution tree
            node(pneumaticDuct, ectorumSector.copy().add(new Research(powerTransmitter)), () -> {
                node(pneumaticDuctRouter, () -> {
                    node(pneumaticDuctBridge, () -> {
                        node(armoredPneumaticDuct, () -> {

                        });
                    });

                    node(pneumaticOverflowDuct, () -> {
                        node(pneumaticUnderflowDuct, () -> {

                        });
                    });
                });
            });

            //Drills
            node(impulseBore, ectorumSector.copy().add(new Research(geyserTurbine)), () -> {
            });


            //Power, Production, Liquid distribution tree
            node(geyserTurbine, ectorumSector, () -> {
                node(powerTransmitter, Seq.with(new Research(impulseBore)), () -> {
                    node(zincBattery, () -> {
                        node(zincBattery, () -> {

                        });
                    });
                });

                node(cliffShredder, Seq.with(new OnSector(wasteland)), () -> {
                    node(highPressureSiliconSmelter, () -> {

                    });
                });

                node(compoundAssembler, Seq.with(new OnSector(foothills)), () -> {

                });
            });

            //Turrets and walls tree
            node(radarDevice, Seq.with(new Research(powerTransmitter)), () -> {
                node(sentinel, ()  -> {
                    node(bismuthWall, () -> {
                        node(bismuthWallLarge, () -> {

                        });
                    });
                });
            });

            //Cores and imitators tree
            node(coreSparkImitator, Seq.with(new OnSector(elderPlateau)), () -> {
                node(coreSunrise, () -> {

                });
            });

            //Units, unit constructors and unit distribution tree
            node(mechAssemblyUnit, Seq.with(new OnSector(wasteland)), () -> {
                node(EctoUnitTypes.fist);

                node(groundReassemblyUnit, Seq.with(new OnSector(recyclingFacility)), () -> {

                });

                node(spiderAssemblyUnit, Seq.with(new OnSector(ruins)), () -> {
                    node(EctoUnitTypes.echo);
                });

                node(airAssemblyUnit, Seq.with(new OnSector(metalRidge)), () -> {
                    node(EctoUnitTypes.spasm);
                });
            });

            //Sectors tree
            node(theHollow, () -> {

                node(wasteland, Seq.with(
                        new SectorComplete(theHollow),
                        new Research(impulseBore),
                        new Research(powerTransmitter),
                        new Research(sentinel)
                ), () -> {

                    node(foothills, Seq.with(
                            new SectorComplete(wasteland),
                            new Research(highPressureSiliconSmelter)

                            /*
                             * TODO:
                             * , new Research(windProtectionTechnology)
                             */
                    ), () -> {

                        node(ancientCanyon, Seq.with(
                                new SectorComplete(foothills),
                                new Research(compoundAssembler)

                                /*
                                 * TODO:
                                 * , new Research(waterPump)
                                 */
                        ), () -> {

                            node(ruins, Seq.with(
                                    new SectorComplete(ancientCanyon)

                                    /*
                                     * TODO:
                                     * , new Research(repairProjector)
                                     * , new Research(radar)
                                     */
                            ), () -> {

                                /*
                                 * Верхняя/горная ветка:
                                 * Ruins -> Elder Plateau -> Metal Ridge
                                 */
                                node(elderPlateau, Seq.with(
                                        new SectorComplete(ruins)

                                        /*
                                         * TODO:
                                         * , new Research(sulfurProcessor)
                                         * , new Research(coreSignatureImitator)
                                         */
                                ), () -> {

                                    node(metalRidge, Seq.with(
                                            new SectorComplete(elderPlateau)

                                            /*
                                             * TODO:
                                             * , new Research(coreDecoy)
                                             * , new Research(advancedDefense)
                                             */
                                    ), () -> {
                                        /*
                                         * TODO:

                                         */
                                    });
                                });

                                /*
                                 * Серная ветка:
                                 * Ruins -> Dried River -> Sulfuric Swamp -> Volcanic Land
                                 */
                                node(driedRiver, Seq.with(
                                        new SectorComplete(ruins)

                                        /*
                                         * TODO:
                                         * , new Research(sulfurProcessing)
                                         */
                                ), () -> {

                                    node(sulfuricSwamp, Seq.with(
                                            new SectorComplete(driedRiver)

                                            /*
                                             * TODO:
                                             * , new Research()
                                             */
                                    ), () -> {

                                        node(recyclingFacility, Seq.with(
                                                new SectorComplete(metalRidge),
                                                new SectorComplete(sulfuricSwamp)

                                                /*
                                                 * TODO:
                                                 * , new Research(chromium)
                                                 * , new Research(recyclingTechnology)
                                                 */
                                        ), () -> {
                                            /*
                                             * Финал первого технологического этапа.
                                             */
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });

            // Items and liquids tree
            nodeProduce(EctoItems.bismuth, () -> {
                nodeProduce(EctoItems.zinc, () -> {
                });
                nodeProduce(Items.sand, () -> {
                    nodeProduce(Items.scrap, () -> {
                    });
                    nodeProduce(Items.graphite, () -> {
                        nodeProduce(EctoItems.lithium, () -> {

                        });
                    });
                    nodeProduce(Items.silicon, () -> {
                        nodeProduce(EctoItems.hydrodefensiveCompound, () -> {
                        });
                    });
                });
                nodeProduce(Liquids.water, () -> {
                });
                nodeProduce(EctoItems.sulfur, () -> {
                    nodeProduce(EctoLiquids.sulfurSolution, () -> {
                        nodeProduce(EctoLiquids.sulfuricAcid, () -> {
                            nodeProduce(EctoItems.teynorite, () -> {
                                nodeProduce(EctoItems.chromium, () -> {

                                });
                            });
                        });
                    });
                    nodeProduce(EctoItems.sulfide, () -> {
                    });
                });
            });
        });
    }
}