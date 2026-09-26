// Было: 4 ObjectMap + дублирование alt

// Стало:
package ectotech.graphics;

import arc.Core;
import arc.graphics.Texture;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Font.Glyph;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Label;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import ectotech.content.EctoPlanets;
import mindustry.Vars;
import mindustry.gen.Iconc;
import mindustry.type.Item;
import mindustry.type.Planet;
import mindustry.ui.Fonts;

public class EctoVanillaSpritesSwapper {
    private static final ObjectMap<Item, SpritePair> sprites = new ObjectMap<>();
    private static boolean isOverrideActive;

    private static class SpritePair {
        TextureRegion originalFull;
        TextureRegion originalUi;
        TextureRegion alternative;

        int character;
        Glyph originalDef;
        Glyph originalOutline;
        Glyph alternativeDef;
        Glyph alternativeOutline;

        boolean hasFontIcon() {
            return originalDef != null && originalOutline != null
                    && alternativeDef != null && alternativeOutline != null;
        }
    }

    public static void register(Item item, String altRegion) {
        if (sprites.containsKey(item)) {
            Log.warn("EctoTech: предмет @ уже зарегистрирован для замены иконки.", item.name);
            return;
        }

        SpritePair pair = new SpritePair();
        pair.originalFull = new TextureRegion(item.fullIcon);
        pair.originalUi = new TextureRegion(item.uiIcon);
        pair.alternative = Core.atlas.find("ectotech-" + altRegion);

        if (!pair.alternative.found()) {
            Log.warn("EctoTech: не найдена альтернативная иконка: @", "ectotech-" + altRegion);
        } else {
            prepareFontIcon(item, pair);
        }

        sprites.put(item, pair);

        if (isOverrideActive) {
            apply(item, pair, true);
            invalidateLabels();
        }
    }

    private static void prepareFontIcon(Item item, SpritePair pair) {
        pair.character = Fonts.getUnicode(item.name);

        if (pair.character == 0 || Fonts.def == null || Fonts.outline == null) {
            Log.warn("EctoTech: у предмета @ не найден зарегистрированный текстовый символ.", item.name);
            return;
        }

        pair.originalDef = Fonts.def.getData().getGlyph((char)pair.character);
        pair.originalOutline = Fonts.outline.getData().getGlyph((char)pair.character);

        if (pair.originalDef == null || pair.originalOutline == null) {
            Log.warn("EctoTech: у предмета @ нет глифа в основном или контурном шрифте.", item.name);
            return;
        }

        pair.alternativeDef = alternativeGlyph(Fonts.def, pair.originalDef, pair.alternative);
        pair.alternativeOutline = alternativeGlyph(Fonts.outline, pair.originalOutline, pair.alternative);

        if (Iconc.codes.containsKey(item.name)) {
            Log.warn("EctoTech: :@: в UI.formatIcons сначала обрабатывается как Iconc; " + "замена предметного глифа может не затронуть эту запись.", item.name);
        }
    }

    /** Находит страницу с текстурой картинки или добавляет её шрифту. */
    private static int pageFor(Font font, Texture texture) {
        Seq<TextureRegion> pages = font.getRegions();

        for (int i = 0; i < pages.size; i++) {
            if (pages.get(i).texture == texture) return i;
        }

        pages.add(new TextureRegion(texture));
        return pages.size - 1;
    }

    /**
     * Новый рисунок для того же символа.
     * Размеры и отступы сохраняем: переключение не меняет ширину текста.
     */
    private static Glyph alternativeGlyph(Font font, Glyph original, TextureRegion region) {
        Glyph glyph = new Glyph();

        glyph.id = original.id;
        glyph.srcX = original.srcX;
        glyph.srcY = original.srcY;
        glyph.width = original.width;
        glyph.height = original.height;
        glyph.xoffset = original.xoffset;
        glyph.yoffset = original.yoffset;
        glyph.xadvance = original.xadvance;
        glyph.kerning = original.kerning;
        glyph.fixedWidth = original.fixedWidth;

        glyph.page = pageFor(font, region.texture);
        glyph.u = region.u;
        glyph.v = region.v2;
        glyph.u2 = region.u2;
        glyph.v2 = region.v;

        return glyph;
    }

    private static void apply(Item item, SpritePair pair, boolean ectorum) {
        if (ectorum) {
            if (!pair.alternative.found()) return;

            item.fullIcon.set(pair.alternative);
            item.uiIcon.set(pair.alternative);
        } else {
            item.fullIcon.set(pair.originalFull);
            item.uiIcon.set(pair.originalUi);
        }

        if (pair.hasFontIcon()) {
            Fonts.def.getData().setGlyph(
                    pair.character,
                    ectorum ? pair.alternativeDef : pair.originalDef
            );

            Fonts.outline.getData().setGlyph(
                    pair.character,
                    ectorum ? pair.alternativeOutline : pair.originalOutline
            );
        }
    }

    public static void apply(boolean isEctorum) {
        if (isOverrideActive == isEctorum) return;

        for (var entry : sprites) {
            apply(entry.key, entry.value, isEctorum);
        }

        isOverrideActive = isEctorum;
        invalidateLabels();
    }

    private static void invalidateLabels() {
        if (Core.scene == null || Core.scene.root == null) return;

        Core.scene.root.forEach(element -> {
            if (element instanceof Label label) label.invalidate();
        });
    }



    public static void updateUI() {
        Runnable restoreFromGame = () -> {
            boolean ectorum = Vars.state.isPlaying() && Vars.state.rules.planet == EctoPlanets.ectorum;
            apply(ectorum);
        };

        Vars.ui.database.update(() -> {
            if (!Vars.ui.database.isShown()) return;
            Planet planet = EctoUiPlanetReader.fromDatabase(Vars.ui.database);
            apply(planet == EctoPlanets.ectorum);
        });
        Vars.ui.database.hidden(restoreFromGame);

        Vars.ui.research.update(() -> {
            if (!Vars.ui.research.isShown()) return;
            Planet planet = EctoUiPlanetReader.fromResearch(Vars.ui.research);
            apply(planet == EctoPlanets.ectorum);
        });
        Vars.ui.research.hidden(restoreFromGame);
    }
 }