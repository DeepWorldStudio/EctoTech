package ectotech.content;

import arc.graphics.Color;
import mindustry.content.Planets;
import mindustry.game.Rules;
import mindustry.graphics.g3d.HexMesh;
import mindustry.graphics.g3d.HexSkyMesh;
import mindustry.graphics.g3d.MultiMesh;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.Planet;
import mindustry.world.meta.Env;

import ectotech.EctoTech;

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
            allowLaunchSchematics = true;
            allowSectorInvasion = true;
            allowWaves = true;
            clearSectorOnLose = true;

            startSector = 0;
            defaultCore = EctoBlocks.coreSpark;

            defaultEnv = Env.terrestrial | Env.oxygen | Env.groundWater;

            ruleSetter = (Rules r) -> {
                if (EctoTech.ectoTeam != null) {
                    r.waveTeam = EctoTech.ectoTeam;
                }

                r.placeRangeCheck = false;

                r.coreDestroyClear = true;
            };
        }};
    }
}