package ectotech.content;

import arc.graphics.*;
import arc.struct.*;
import mindustry.type.*;

public class EctoItems {
    // Этап 1
    public static Item bismuth, zinc, sulfur,
            hydrodefensiveCompound;

    public static void load() {
        // Этап 1
        bismuth = new Item("bismuth", Color.valueOf("5db56a")) {{
            hardness = 1;
            cost = 0.5f;
        }};

        zinc = new Item("zinc", Color.valueOf("d4d4d4")) {{
            hardness = 1;
            cost = 0.6f;
        }};

        sulfur = new Item("sulfur", Color.valueOf("e8d84a")) {{
            hardness = 2;
            cost = 0.7f;
            flammability = 0.3f;
        }};

        hydrodefensiveCompound = new Item("hydrodefensive-compound", Color.valueOf("7a9fb5")) {{
            cost = 1.5f;
        }};

    }
}