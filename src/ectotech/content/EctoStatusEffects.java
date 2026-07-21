package ectotech.content;

import arc.graphics.Color;
import mindustry.content.StatusEffects;
import mindustry.type.StatusEffect;

public class EctoStatusEffects {
    public static StatusEffect quicksandStuck;

    public static void load() {

        quicksandStuck = new StatusEffect("quicksand-stuck") {{
            color = Color.valueOf("F2B16F");
            speedMultiplier = 0.7f;
            reloadMultiplier = 0.6f;
            damageMultiplier = 0.8f;
            dragMultiplier = 1.4f;

            effect = EctoFx.quicksandPulverizeSmall;

            init(() -> {
                opposites.add(StatusEffects.wet);
            });
        }};
    }
}

