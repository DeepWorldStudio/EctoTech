package ectotech.content;

import arc.graphics.Color;
import mindustry.type.Liquid;
import mindustry.content.StatusEffects;

public class EctoLiquids {

    // Этап 1
    public static Liquid sulfurSolution, sulfuricAcid;

    public static void load() {

        sulfurSolution = new Liquid("sulfur-solution", Color.valueOf("c8b04a")) {{
            coolant = false;
            viscosity = 0.70f;
            temperature = 0.55f;
            heatCapacity = 0.35f;

            lightColor = Color.valueOf("c8b04a22");
        }};

        sulfuricAcid = new Liquid("sulfuric-acid", Color.valueOf("d8d26a")) {{
            coolant = false;
            viscosity = 0.85f;
            temperature = 0.65f;
            heatCapacity = 0.25f;

            effect = StatusEffects.corroded;
        }};
    }
}