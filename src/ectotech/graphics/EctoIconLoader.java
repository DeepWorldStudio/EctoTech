package ectotech.graphics;

import arc.graphics.Texture;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Font.Glyph;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.Vars;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.ui.Fonts;

public final class EctoIconLoader{
    private static final String modName = "ectotech";
    private static final int minCode = 0xE000;
    private static final int maxCode = 0xF8FF;

    private static int nextCode = minCode;
    private static boolean loaded;

    private EctoIconLoader(){}

    public static void load() {
        if (loaded || Vars.headless) return;

        if (Fonts.def == null || Fonts.outline == null) {
            Log.warn("EctoTech: fonts are not loaded yet; content icons were not registered.");
            return;
        }

        loaded = true;

        int count = 0;

        for (var type : ContentType.all) {
            for (var content : Vars.content.getBy(type)) {
                if (content instanceof UnlockableContent uc && isEcto(uc)) {
                    if (register(uc)) count++;
                }
            }
        }

        Log.info("EctoTech: registered @ content text icons.", count);
    }

    private static boolean isEcto(UnlockableContent content) {
        return content.minfo.mod != null
                && content.minfo.mod.name.equals(modName);
    }

    private static boolean register(UnlockableContent content) {
        if (Fonts.hasUnicodeStr(content.name)) return false;

        TextureRegion region = content.uiIcon;
        if (region == null || !region.found()) {
            Log.warn("EctoTech: UI icon for '@' was not found.", content.name);
            return false;
        }

        int code = nextFreeCode();
        if (code == -1) {
            Log.err("EctoTech: no free private Unicode code points.");
            return false;
        }

        String regionName = region instanceof AtlasRegion atlas
                ? atlas.name
                : content.name;

        /*
         * Регистрирует соответствия:
         * content.name -> Unicode;
         * Unicode -> имя региона;
         * content.name -> строка с символом.
         *
         * Созданные registerIcon() глифы имеют page = 0,
         * поэтому ниже они сразу заменяются корректными.
         */
        Fonts.registerIcon(content.name, regionName, code, region);

        setGlyph(Fonts.def, code, region);
        setGlyph(Fonts.outline, code, region);

        // Не обязательно для :name: в обычном тексте,
        // но полезно при прямом использовании Fonts.icon.
        if (Fonts.icon != null) {
            setGlyph(Fonts.icon, code, region);
        }

        return true;
    }

    private static void setGlyph(Font font, int code, TextureRegion region) {
        int page = getTexturePage(font, region.texture);
        int size = (int) (font.getData().lineHeight / font.getData().scaleY);
        Vec2 fit = Scaling.fit.apply(region.width, region.height, size, size);

        Glyph glyph = new Glyph();
        glyph.id = code;
        glyph.srcX = 0;
        glyph.srcY = 0;
        glyph.width = (int) fit.x;
        glyph.height = (int) fit.y;

        glyph.u = region.u;
        glyph.v = region.v2;
        glyph.u2 = region.u2;
        glyph.v2 = region.v;

        glyph.xoffset = (size - glyph.width) / 2;
        glyph.yoffset = (size - glyph.height) / 2 - size;
        glyph.xadvance = size;

        glyph.kerning = null;
        glyph.fixedWidth = true;

        // Главное отличие от Fonts.registerIcon():
        // используем реальную страницу текстуры иконки.
        glyph.page = page;

        font.getData().setGlyph(code, glyph);
    }

    private static int getTexturePage(Font font, Texture texture) {
        for (int i = 0; i < font.getRegions().size; i++) {
            if (font.getRegion(i).texture == texture) {
                return i;
            }
        }

        /*
         * Пиксели и текстура не копируются.
         * В Font добавляется только ссылка на уже существующую
         * текстуру атласа.
         */
        font.getRegions().add(new TextureRegion(texture));
        return font.getRegions().size - 1;
    }

    private static int nextFreeCode() {
        while (nextCode <= maxCode) {
            int code = nextCode++;

            if (Fonts.unicodeToName(code) != null) continue;
            if (hasGlyph(Fonts.def, code)) continue;
            if (hasGlyph(Fonts.outline, code)) continue;
            if (Fonts.icon != null && hasGlyph(Fonts.icon, code)) continue;

            return code;
        }

        return -1;
    }

    private static boolean hasGlyph(Font font, int code) {
        return font.getData().getGlyph((char) code) != null;
    }
}