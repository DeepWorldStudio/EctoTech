// Было: 4 ObjectMap + дублирование alt

// Стало:
package ectotech.graphics;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.struct.ObjectMap;
import arc.util.Log;
import ectotech.content.EctoPlanets;
import mindustry.Vars;
import mindustry.type.Item;
import mindustry.type.Planet;

public class EctoVanillaSpritesSwapper {
    private static final ObjectMap<Item, TextureRegion> originalFull = new ObjectMap<>();
    private static final ObjectMap<Item, TextureRegion> originalUi   = new ObjectMap<>();
    private static final ObjectMap<Item, TextureRegion> altSprites   = new ObjectMap<>();

    private static boolean isOverrideActive = false;

    public static void register(Item item, String altRegion) {
        originalFull.put(item, new TextureRegion(item.fullIcon));
        originalUi.put(item,   new TextureRegion(item.uiIcon));

        TextureRegion alt = Core.atlas.find("ectotech-" + altRegion);
        altSprites.put(item, alt);

        if (!alt.found()) {
            Log.warn("EctoTech: vanilla sprite override region not found: @", "ectotech-" + altRegion);
        }
    }

    public static void apply(boolean isEctorum) {
        if (isOverrideActive == isEctorum) return;

        for (var entry : altSprites) {
            Item item = entry.key;

            if (isEctorum) {
                TextureRegion alt = entry.value;
                if (alt.found()) {
                    item.fullIcon.set(alt);
                    item.uiIcon.set(alt);
                }
            } else {
                item.fullIcon.set(originalFull.get(item));
                item.uiIcon.set(originalUi.get(item));
            }
        }

        isOverrideActive = isEctorum;
    }

    /**Updates UI and Database alt icons*/
    public static void updateUI() {
        Runnable restoreFromGame = () -> {
            boolean ectorum = Vars.state.isPlaying() && Vars.state.rules.planet == EctoPlanets.ectorum;
            EctoVanillaSpritesSwapper.apply(ectorum);
        };

        Vars.ui.database.update(() -> {
            if (!Vars.ui.database.isShown()) return;
            Planet p = EctoUiPlanetReader.fromDatabase(Vars.ui.database);
            EctoVanillaSpritesSwapper.apply(p == EctoPlanets.ectorum);
        });
        Vars.ui.database.hidden(restoreFromGame);

        Vars.ui.research.update(() -> {
            if (!Vars.ui.research.isShown()) return;
            Planet p = EctoUiPlanetReader.fromResearch(Vars.ui.research);
            EctoVanillaSpritesSwapper.apply(p == EctoPlanets.ectorum);
        });
        Vars.ui.research.hidden(restoreFromGame);
    }
 }