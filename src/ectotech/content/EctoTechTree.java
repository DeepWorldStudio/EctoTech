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
        EctoPlanets.ectorum.techTree = nodeRoot("ectorum", coreSpark, () -> {
            node(theHollow, () -> {

                node(wasteland, Seq.with(
                        new SectorComplete(theHollow)
                        // TODO, когда появятся реальные блоки: new Research(graphitePress) new Research(basicDrill)
                ), () -> {

                    node(foothills, Seq.with(
                            new SectorComplete(wasteland)

                            /*
                             * TODO:
                             * , new Research(siliconSmelter)
                             * , new Research(windProtectionTechnology)
                             */
                    ), () -> {

                        node(ancientCanyon, Seq.with(
                                new SectorComplete(foothills)

                                /*
                                 * TODO:
                                 * , new Research(waterPump)
                                 * , new Research(basicUnitFactory)
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
                                         * Здесь позже можно открыть литиевую ветку:
                                         *
                                         * node(lithiumExtractor, Seq.with(
                                         *     new SectorComplete(metalRidge)
                                         * ), () -> {});
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
                                             * , new Research(sulfuricAcid)
                                             */
                                    ), () -> {

                                        node(volcanicLand, Seq.with(
                                                new SectorComplete(sulfuricSwamp)

                                                /*
                                                 * TODO:
                                                 * , new Research(sulfide)
                                                 * , new Research(hydrodefensiveCompound)
                                                 */
                                        ), () -> {

                                            node(recyclingFacility, Seq.with(
                                                    new SectorComplete(sulfuricSwamp),
                                                    new SectorComplete(volcanicLand)

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

            nodeProduce(bismuth, () -> {
                nodeProduce(zinc, () -> {
                });
                nodeProduce(sand, () -> {
                    nodeProduce(scrap, () -> {
                    });
                    nodeProduce(graphite, () -> {
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

                        });
                    });
                    nodeProduce(sulfide, () -> {
                    });
                });
            });
        });
    }
}