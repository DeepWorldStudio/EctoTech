package ectotech.content;

import arc.struct.Seq;
import ectotech.type.EctorumSectorPreset;
import mindustry.content.Weathers;
import mindustry.type.SectorPreset;
import mindustry.type.Weather;

import static ectotech.content.EctoPlanets.ectorum;

public class EctoSectorPresets {
    public static SectorPreset

    // Start
    theHollow,
    wasteland, foothills, ancientCanyon, ruins,

    // first fork
    elderPlateau, metalRidge, cheerlessValley, /*TODO Угрюмая/Мрачная долина*/ sinisterCity /*TODO Зловещий город*/,
    driedRiver, sulfuricSwamp, recyclingFacility;

    public static void load() {
        // 1. Полость (The Hollow) - стартовый сектор
        theHollow = new SectorPreset("the-hollow", ectorum, 92) {{
            alwaysUnlocked = true;
            addStartingItems = true;
            captureWave = 12;
            difficulty = 1;
            overrideLaunchDefaults = true;

            rules = r -> {
                r.winWave = this.captureWave;
                r.hideSpawns = false;
                r.weather = Seq.with(new Weather.WeatherEntry(Weathers.fog) {{
                    intensity = 3f;
                    always = true;
                }});

            };
        }};

        // 2. Пустошь (Wasteland)
        wasteland = new EctorumSectorPreset("wasteland", ectorum, 273) {{
            difficulty = 1;
        }};

        // 3. Предгорья (Foothills)
        foothills = new SectorPreset("foothills", ectorum, 93) {{
            captureWave = 25;
            difficulty = 3;
        }};

        // 4. Древний каньон (Ancient Canyon)
        ancientCanyon = new SectorPreset("ancient-canyon", ectorum, 453) {{
            captureWave = 30;
            difficulty = 4;
        }};

        // 5. Развалины (Ruins)
        ruins = new SectorPreset("ruins", ectorum, 154) {{
            captureWave = 35;
            difficulty = 4;
        }};

        // 6.1. Старое плато (Elder Plateau)
        elderPlateau = new SectorPreset("elder-plateau", ectorum, 462) {{
            captureWave = 40;
            difficulty = 5;
            startWaveTimeMultiplier = 1.5f;
        }};

        // 6.2. Пересохшая река (Dried River)
        driedRiver = new SectorPreset("dried-river", ectorum, 335) {{
            captureWave = 40;
            difficulty = 5;
        }};

        // 7.1. Металлический хребет (Metal Ridge)
        metalRidge = new SectorPreset("metal-ridge", ectorum, 153) {{
            captureWave = 50;
            difficulty = 6;
        }};

        // 7.2. Серное болото (Sulfuric Swamp)
        sulfuricSwamp = new SectorPreset("sulfuric-swamp", ectorum, 336) {{
            captureWave = 50;
            difficulty = 6;
        }};

        // 8. Перерабатывающий завод (Recycling Facility)
        recyclingFacility = new SectorPreset("recycling-facility", ectorum, 337) {{
            captureWave = 70;
            difficulty = 8;
            isLastSector = true;
        }};
    }
}