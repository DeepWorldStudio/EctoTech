package ectotech.graphics;

import arc.util.Reflect;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Planet;
import mindustry.ui.dialogs.DatabaseDialog;
import mindustry.ui.dialogs.ResearchDialog;

public class EctoUiPlanetReader {

    public static Planet fromDatabase(DatabaseDialog dialog) {
        try {
            UnlockableContent tab = Reflect.get(dialog, "tab");
            return tab instanceof Planet planet ? planet : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static Planet fromResearch(ResearchDialog dialog) {
        if (dialog.lastNode == null) return null;

        for (Planet planet : Vars.content.planets()) {
            if (planet.techTree == dialog.lastNode || dialog.lastNode.planet == planet) {
                return planet;
            }
        }

        return null;
    }
}