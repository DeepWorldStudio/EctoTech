package ectotech.content;

import arc.graphics.*;
import arc.struct.*;
import mindustry.type.*;

public class EctoItems {

    public static Item
            // Этап 1
            bismuth, zinc, sulfur, hydrodefensiveCompound, sulfide,
            // Этап 2
            teynorite, chromium, lithium, electrosteel,
            // Этап 3
            borum, magnetite,
            // Этап 4
            kyanite, refinedRadiance;


    public static void load() {

        // Этап 1
        bismuth = new Item("bismuth", Color.valueOf("5db56a")) {{
            hardness = 1;
            cost = 0.8f;
        }};

        zinc = new Item("zinc", Color.valueOf("d4d4d4")) {{
            hardness = 1;
            cost = 0.6f;
        }};

        sulfur = new Item("sulfur", Color.valueOf("e8d84a")) {{
            hardness = 2;
            cost = 0.7f;
            flammability = 0.3f;
            buildable = false;
        }};

        hydrodefensiveCompound = new Item("hydrodefensive-compound", Color.valueOf("7a9fb5")) {{
            cost = 1.2f;
        }};

        sulfide = new Item("sulfide", Color.valueOf("A2BA27")) {{

        }};

        // Этап 2
        teynorite = new Item("teynorite", Color.valueOf("353240")) {{
            hardness = 3;
            cost = 1.2f;
        }};

        chromium = new Item("chromium", Color.valueOf("424D69")) {{
            cost = 1.6f;
        }};

        lithium = new Item("lithium", Color.valueOf("423F33")) {{
            hardness = 3;
            cost = 1.4f;
        }};

        // Этап 3

        borum = new Item("borum", Color.valueOf("517341")) {{
            hardness = 4;
        }};

        // Этап 4

        kyanite = new Item("kyanite", Color.valueOf("87D5D6")) {{
            hardness = 5;
            healthScaling = 0.2f;
        }};
    }
}