package ectotech.content;

import mindustry.type.SectorPreset;

import static ectotech.content.EctoPlanets.ectorum;

public class EctoSectorPresets {
    public static SectorPreset

    // Start
    theHollow,
    wasteland, foothills, ancientCanyon, ruins,

    // first fork
    elderPlateau, metalRidge,
    driedRiver, sulfuricSwamp, volcanicLand,

    // first fork concatenation
    recyclingFacility;



    public static void load() {
        // 1. Полость (The Hollow) - стартовый сектор
        theHollow = new SectorPreset("the-hollow", ectorum, 15) {{
            alwaysUnlocked = true;
            addStartingItems = true;
            captureWave = 15;
            difficulty = 1;
            overrideLaunchDefaults = true;
        }};

        // 2. Пустошь (Wasteland)
        wasteland = new SectorPreset("wasteland", ectorum, 112) {{
            captureWave = 20;
            difficulty = 2;
        }};

        // 3. Предгорья (Foothills)
        foothills = new SectorPreset("foothills", ectorum, 156) {{
            captureWave = 25;
            difficulty = 3;
        }};

        // 4. Древний каньон (Ancient Canyon)
        ancientCanyon = new SectorPreset("ancient-canyon", ectorum, 201) {{
            captureWave = 30;
            difficulty = 4;
        }};

        // 5. Развалины (Ruins)
        ruins = new SectorPreset("ruins", ectorum, 18) {{
            captureWave = 35;
            difficulty = 4;
        }};

        // 6.1. Старое плато (Elder Plateau)
        elderPlateau = new SectorPreset("elder-plateau", ectorum, 130) {{
            captureWave = 40;
            difficulty = 5;
        }};

        // 6.2. Пересохшая река (Dried River)
        driedRiver = new SectorPreset("dried-river", ectorum, 95) {{
            captureWave = 40;
            difficulty = 5;
        }};

        // 7.1. Металлический хребет (Metal Ridge)
        metalRidge = new SectorPreset("metal-ridge", ectorum, 170) {{
            captureWave = 50;
            difficulty = 6;
        }};

        // 7.2. Серное болото (Sulfuric Swamp)
        sulfuricSwamp = new SectorPreset("sulfuric-swamp", ectorum, 54) {{
            captureWave = 50;
            difficulty = 6;
        }};

        // 8.1. Привулканье (Volcanic Land)
        volcanicLand = new SectorPreset("volcanic-land", ectorum, 72) {{
            captureWave = 60;
            difficulty = 7;
        }};

        // 9. Перерабатывающий завод (Recycling Facility)
        recyclingFacility = new SectorPreset("recycling-facility", ectorum, 140) {{
            captureWave = 70;
            difficulty = 8;
            isLastSector = true;
        }};
    }
}