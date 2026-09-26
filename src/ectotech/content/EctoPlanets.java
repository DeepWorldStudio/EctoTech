package ectotech.content;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Mesh;
import arc.graphics.g3d.Camera3D;
import ectotech.EctoTech;
import ectotech.game.EctoCampaignRules;
import ectotech.world.gen.EctorumPlanetGenerator;
import mindustry.content.Planets;
import mindustry.graphics.g3d.*;
import mindustry.type.Planet;
import mindustry.world.meta.Env;

public class EctoPlanets {

    public static Planet ectorum;

    private static Mesh atmosphereProxy;

    public static void load() {
        ectorum = new Planet("ectorum", Planets.sun, 1.35f, 4) {
            {
                generator = new EctorumPlanetGenerator();

                meshLoader = () -> new HexMesh(this, 6);

                cloudMeshLoader = () -> new MultiMesh(
                        // медленный, редкий, рваный, крупными пятнами — грозовые поля
                        new HexSkyMesh(this, 7,  0.06f, 0.11f,  5, Color.valueOf("5b4a7a").a(0.55f), 3, 0.6f,  1.4f, 0.50f),
                        // быстрый, тонкий, мелкий — перистые полосы над ним
                        new HexSkyMesh(this, 23, 0.32f, 0.155f, 5, Color.valueOf("d9d1e6").a(0.22f), 2, 0.4f,  2.2f, 0.56f)
                );

                // Внешний вид/атмосфера
                hasAtmosphere = true;
                atmosphereColor = Color.valueOf("2F4E70");
                atmosphereRadIn  = 0.02f;
                atmosphereRadOut = 0.46f;
                clipRadius = 2.5f;

                iconColor = Color.valueOf("a47ac4");
                landCloudColor = iconColor.cpy().a(0.45f);

                // Камера в планетарном UI
                minZoom = 0.50f;
                maxZoom = 2.50f;

                lightSrcTo = 0.5f;
                lightDstFrom = 0.2f;

                // Доступность в кампании
                alwaysUnlocked = true;
                accessible = true;
                visible = true;

                allowLaunchToNumbered = false;
                allowSelfSectorLaunch = false;

                // Эти возможности включены у планеты,
                // но сюжетные пресеты переопределят их.
                allowLaunchLoadout = true;
                allowLaunchSchematics = true;

                allowSectorInvasion = false;
                allowWaves = true;
                clearSectorOnLose = true;

                allowCampaignRules = true;

                campaignRuleDefaults = new EctoCampaignRules() {{
                    fog = true;
                    clearFogOnCapture = true;
                }};

                campaignRules = new EctoCampaignRules();

                startSector = 92;
                defaultCore = EctoBlocks.coreSpark;

                defaultEnv = Env.terrestrial | Env.oxygen | Env.groundWater;

                ruleSetter = r -> {
                    if (EctoTech.ectorumTeam != null) r.waveTeam = EctoTech.ectorumTeam;

                    r.placeRangeCheck = false;
                    r.coreDestroyClear = true;
                    r.allowCoreUnloaders = false;
                    r.onlyDepositCore = true;
                    r.hideSpawns = false;
                };
            }

            @Override
            public void loadRules() {
                campaignRules = Core.settings.getJson(name + "-campaign-rules", EctoCampaignRules.class, () -> campaignRules instanceof EctoCampaignRules ? (EctoCampaignRules) campaignRules : new EctoCampaignRules());
            }

            @Override
            public void drawAtmosphere(Mesh ignored, Camera3D cam) {
                float proxyRadius = Math.max(
                        radius + atmosphereRadOut + 0.05f,
                        PlanetRenderer.outlineRad * radius + 0.05f
                );

                if (atmosphereProxy == null) {
                    atmosphereProxy = MeshBuilder.buildHex(Color.white, 2, proxyRadius);
                }

                super.drawAtmosphere(atmosphereProxy, cam);
            }

            @Override
            public void removeContent() {
                if (atmosphereProxy != null) {
                    atmosphereProxy.dispose();
                    atmosphereProxy = null;
                }
                super.removeContent();
            }
        };
    }
}