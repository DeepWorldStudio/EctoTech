package ectotech.content;

import arc.Core;
import arc.graphics.Color;
import arc.struct.Seq;
import ectotech.EctoTech;
import ectotech.game.EctoCampaignRules;
import mindustry.content.Planets;
import mindustry.content.Weathers;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.Planet;
import mindustry.type.Weather;
import mindustry.world.meta.Env;

public class EctoPlanets {

    public static Planet ectorum;

    private static final Color cMain = Color.valueOf("a47ac4");   // основной фиолетовый
    private static final Color cDark = Color.valueOf("3a245d");   // тёмный серо-фиолетовый
    private static final Color cCloud1 = Color.valueOf("b49ac9"); // облака слой 1
    private static final Color cCloud2 = Color.valueOf("e7e0f0"); // облака слой 2

    public static void load() {
        ectorum = new Planet("ectorum", Planets.sun, 1.35f, 4) {{
                generator = new SerpuloPlanetGenerator();

                meshLoader = () -> new HexMesh(this, 6);

                cloudMeshLoader = () -> new MultiMesh(
                        new HexSkyMesh(this,
                                11,
                                0.15f,
                                0.13f,
                                5,
                                cCloud1.cpy().a(0.40f),
                                2,
                                0.45f,
                                0.90f,
                                0.38f
                        ),
                        new HexSkyMesh(this,
                                1,
                                0.60f,
                                0.16f,
                                5,
                                cCloud2.cpy().a(0.25f),
                                2,
                                0.45f,
                                1.00f,
                                0.41f
                        )
                );

                // Внешний вид/атмосфера
                hasAtmosphere = true;
                atmosphereColor = cDark;
                atmosphereRadIn = 0.02f;
                atmosphereRadOut = 0.35f;

                iconColor = cMain;
                landCloudColor = cMain.cpy().a(0.45f);

                // Камера в планетарном UI
                minZoom = 0.50f;
                maxZoom = 4.00f;

                // Доступность в кампании
                alwaysUnlocked = true;
                accessible = true;
                visible = true;

                allowLaunchToNumbered = false;
                allowSelfSectorLaunch = false;

                // Кампания/запуски
                allowLaunchLoadout = true;
                allowLaunchSchematics = false;
                allowSectorInvasion = false;
                allowWaves = true;
                clearSectorOnLose = true;

                allowCampaignRules = true;

                campaignRuleDefaults.fog = true;

                startSector = 15;
                defaultCore = EctoBlocks.coreSpark;

                defaultEnv = Env.terrestrial | Env.oxygen | Env.groundWater;

                ruleSetter = r -> {
                    if (EctoTech.ectorumTeam != null) r.waveTeam = EctoTech.ectorumTeam;

                    r.placeRangeCheck = false;
                    r.coreDestroyClear = true;
                    r.allowCoreUnloaders = false;
                    r.onlyDepositCore = true;
                    r.hideSpawns = false;

                    if (r.sector != null && r.sector.preset == EctoSectorPresets.theHollow) {
                        r.weather = Seq.with(new Weather.WeatherEntry(Weathers.fog));

                        EctoSectorPresets.applyWeather(r, Weathers.fog);
                    }
                };
            }

            @Override
            public void loadRules() {
                var fallback = campaignRules;
                campaignRules = Core.settings.getJson(name + "-campaign-rules", EctoCampaignRules.class, () -> fallback instanceof EctoCampaignRules ? (EctoCampaignRules) fallback : new EctoCampaignRules()
                );
            }
        };
    }
}