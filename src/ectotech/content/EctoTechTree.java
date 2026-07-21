package ectotech.content;

import arc.struct.Seq;
import mindustry.game.Objectives.*;

import static mindustry.content.Items.*;
import static mindustry.content.Liquids.*;
import static mindustry.content.TechTree.*;

import static ectotech.content.EctoItems.*;
import static ectotech.content.EctoLiquids.*;
import static ectotech.content.EctoBlocks.*;
import static ectotech.content.EctoSectorPresets.*;


public class EctoTechTree {
    public static void load() {
        Seq<Objective> ectorumSector = Seq.with(new OnPlanet(EctoPlanets.ectorum));

        EctoPlanets.ectorum.techTree = nodeRoot("ectorum", coreSpark, true, () -> {

            // Item distribution tree
            node(pneumaticDuct, ectorumSector, () -> {
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


            //Power, Production, Drills and Liquid distribution tree
            node(clivelite /*TODO: change to Impulse Bore*/, () -> {
                node(cliffShredder, Seq.with(new OnSector(wasteland)), () -> {
                    node(highPressureSiliconSmelter, () -> {

                    });
                });

                node(compoundAssembler, Seq.with(new OnSector(foothills)), () -> {

                });
            });

            //Turrets and walls tree

            //Cores and imitators tree
            node(coreSparkImitator, Seq.with(new OnSector(elderPlateau)), () -> {
                node(coreSunrise, () -> {

                });
            });

            //Units, unit constructors and unit distribution tree

            //Sectors tree
            node(theHollow, () -> {

                node(wasteland, Seq.with(
                        new SectorComplete(theHollow)
                        // TODO new Research(ImpulseBore)
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
        });

        // Items and liquids tree
        nodeProduce(bismuth, () -> {
            nodeProduce(zinc, () -> {
            });
            nodeProduce(sand, () -> {
                nodeProduce(scrap, () -> {
                });
                nodeProduce(graphite, () -> {
                    nodeProduce(lithium, () -> {

                    });
                });
                nodeProduce(silicon, () -> {
                    nodeProduce(hydrodefensiveCompound, () -> {
                    });
                });
            });
            nodeProduce(water, () -> {
            });
            nodeProduce(sulfur, () -> {
                nodeProduce(sulfurSolution, () -> {
                    nodeProduce(sulfuricAcid, () -> {
                        nodeProduce(teynorite, () -> {
                            nodeProduce(chromium, () -> {

                            });
                        });
                    });
                });
                nodeProduce(sulfide, () -> {
                });
            });
        });
    }
}