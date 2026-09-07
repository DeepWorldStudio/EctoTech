package ectotech.world.blocks.environment;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.util.Nullable;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.MultiPacker.PageType;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

/**Blends liquids together with a standard floor. No new mechanics. Now supports updateEffects*/
public class ParticledShallowLiquid extends ParticledFloor {
    public @Nullable Floor liquidBase, floorBase;
    public float liquidOpacity = 0.35f;

    public ParticledShallowLiquid(String name) {
        super(name);
    }

    public void set(Block liquid, Block floor) {
        liquidBase = liquid.asFloor();
        floorBase = floor.asFloor();

        isLiquid = true;
        variants = floorBase.variants;
        status = liquidBase.status;
        liquidDrop = liquidBase.liquidDrop;
        cacheLayer = liquidBase.cacheLayer;
        shallow = true;

        if (updateEffect == null && liquidBase instanceof ParticledFloor liquidFloor && liquidFloor.updateEffect != null) {
            updateEffect = liquidFloor.updateEffect;
            updateEffectChance = liquidFloor.updateEffectChance;
            effectColor = liquidFloor.effectColor;
            effectSpread = liquidFloor.effectSpread;
        }
    }

    @Override
    public void createIcons(MultiPacker packer) {
        if (liquidBase == null || floorBase == null) return;

        Pixmap overlay = packer.get(liquidBase.name).crop();
        if (overlay == null) return;

        int count = Math.max(floorBase.variants, 1);

        for (int i = 0; i < count; i++) {
            String floorName = floorBase.variants == 0
                    ? floorBase.name
                    : floorBase.name + (i + 1);

            Pixmap base = packer.get(floorName).crop();
            if (base == null) continue;

            for (int x = 0; x < base.width; x++) {
                for (int y = 0; y < base.height; y++) {
                    int mask = overlay.getRaw(x % overlay.width, y % overlay.height);
                    int blended = Pixmap.blend(
                            (mask & 0xffffff00) | (int)(liquidOpacity * 255),
                            base.getRaw(x, y)
                    );
                    base.setRaw(x, y, blended);
                }
            }

            String resultName = (variants <= 1) ? this.name : this.name + (i + 1);
            packer.add(PageType.environment, resultName, base);
            base.dispose();
        }

        super.createIcons(packer);
    }
}