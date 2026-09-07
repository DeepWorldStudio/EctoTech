package ectotech.content;

import arc.audio.Sound;
import mindustry.Vars;

public class EctoSounds {

    public static Sound
            loopGeyserActive,
            loopGeyserInactive,

            geyserEruption, shootEcho;

    public static void load() {

        loopGeyserActive = Vars.tree.loadSound("loops/loopGeyserActive");
        loopGeyserInactive = Vars.tree.loadSound("loops/loopGeyserInactive");
        geyserEruption = Vars.tree.loadSound("environment/geyserEruption");
        shootEcho = Vars.tree.loadSound("shoot/shootEcho");

    }
}
