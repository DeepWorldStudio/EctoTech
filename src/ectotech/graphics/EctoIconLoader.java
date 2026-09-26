package ectotech.graphics;

import arc.Core;
import arc.graphics.Texture;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.Font.Glyph;
import arc.graphics.g2d.TextureAtlas.AtlasRegion;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.scene.Group;
import arc.scene.ui.Label;
import arc.struct.IntMap;
import arc.util.Log;
import arc.util.Nullable;
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
    private static final IntMap<UnlockableContent> registered = new IntMap<>();

    private EctoIconLoader(){}

    public static void install() {
        Core.app.post(() -> {
            EctoIconLoader.load();

            Core.app.post(EctoIconLoader::reload);
        });
    }

    public static void load() {
        if (loaded || Vars.headless) return;

        if (Fonts.def == null || Fonts.outline == null) {
            Log.warn("EctoTech: fonts are not loaded yet; content icons were not registered.");
            return;
        }

        int count = 0;

        for (var type : ContentType.all) {
            for (var content : Vars.content.getBy(type)) {
                if (content instanceof UnlockableContent uc && isEctoTechContent(uc)) {
                    if (register(uc)) count++;
                }
            }
        }

        Log.info("EctoTech: registered @ content text icons.", count);
    }

    public static void reload() {
        if (Vars.headless) return;

        if (!loaded) {
            load();
            return;
        }

        if (Fonts.def == null || Fonts.outline == null) return;

        int count = 0;

        for (var entry : registered) {
            int code = entry.key;
            UnlockableContent content = entry.value;

            if (Fonts.getUnicode(content.name) != code) {
                Log.warn("EctoTech: Unicode mapping for '@' has changed; reload skipped.", content.name);
                continue;
            }

            TextureRegion region = currentIcon(content);
            if (region == null) {
                Log.warn("EctoTech: cannot refresh text icon for '@': no valid UI region.", content.name);
                continue;
            }

            String regionName = region instanceof AtlasRegion atlas ? atlas.name : content.name;

            Fonts.registerIcon(content.name, regionName, code, region);

            setGlyph(Fonts.def, code, region);
            setGlyph(Fonts.outline, code, region);

            if (Fonts.icon != null) {
                setGlyph(Fonts.icon, code, region);
            }

            count++;
        }

        loaded = true;

        if (count > 0 && Core.scene != null) {
            invalidateLabels(Core.scene.root);
        }

        Log.info("EctoTech: refreshed @ content text icons.", count);
    }

    private static boolean register(UnlockableContent content) {
        if (Fonts.hasUnicodeStr(content.name)) return false;

        TextureRegion region = currentIcon(content);
        if (region == null) {
            Log.warn("EctoTech: UI icon for '@' was not found.", content.name);
            return false;
        }

        int code = nextFreeCode();
        if (code == -1) {
            Log.err("EctoTech: no free private Unicode code points.");
            return false;
        }

        String regionName = region instanceof AtlasRegion atlas ? atlas.name : content.name;

        Fonts.registerIcon(content.name, regionName, code, region);

        setGlyph(Fonts.def, code, region);
        setGlyph(Fonts.outline, code, region);

        if (Fonts.icon != null) {
            setGlyph(Fonts.icon, code, region);
        }

        registered.put(code, content);

        return true;
    }

    private static boolean isEctoTechContent(UnlockableContent content) {
        return content.minfo.mod != null && content.minfo.mod.name.equals(modName);
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

        glyph.page = page;

        font.getData().setGlyph(code, glyph);
    }

    private static void invalidateLabels(Group group) {
        for (var element : group.getChildren()) {
            if (element instanceof Label label) label.invalidateHierarchy();
            if (element instanceof Group child) invalidateLabels(child);
        }
    }

    private static int getTexturePage(Font font, Texture texture) {
        for (int i = 0; i < font.getRegions().size; i++) {
            if (font.getRegion(i).texture == texture) {
                return i;
            }
        }

        font.getRegions().add(new TextureRegion(texture));
        return font.getRegions().size - 1;
    }

    private static @Nullable TextureRegion currentIcon(UnlockableContent content) {
        TextureRegion region = content.uiIcon;

        if (region instanceof AtlasRegion atlas && atlas.name != null && Core.atlas != null) {
            region = Core.atlas.find(atlas.name, region);
        }

        if (region == null || !region.found() || region.texture == null || region.texture.isDisposed() || region.width <= 0 || region.height <= 0) {
            return null;
        }
        return region;
    }

    private static int nextFreeCode() {
        while (nextCode <= maxCode) {
            int code = nextCode++;

            if (Fonts.unicodeToName(code) != null || hasGlyph(Fonts.def, code) || hasGlyph(Fonts.outline, code) || hasGlyph(Fonts.icon, code)) continue;
            return code;
        }

        return -1;
    }

    private static boolean hasGlyph(Font font, int code){
        if (font == null) return false;

        var data = font.getData();
        Glyph glyph = data.getGlyph((char)code);

        return glyph != null && glyph != data.missingGlyph;
    }
}