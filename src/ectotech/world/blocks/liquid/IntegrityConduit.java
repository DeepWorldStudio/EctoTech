package ectotech.world.blocks.liquid;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import ectotech.world.modules.IntegrityLeakModule;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.liquid.Conduit;

public class IntegrityConduit extends Conduit {
    /** Доля HP (0..1), ниже которой начинается утечка. */
    public float leakThreshold = 1f;
    /** Доля потока (0..1), теряемая при ~0 HP. */
    public float leakFraction = 0.5f;
    /** Кривая: 1 — линейно, >1 — медленный старт, <1 — резкий. */
    public float leakCurve = 1f;

    /** Повреждается ли блок от собственной утечки. */
    public boolean leakDamages = false;
    /** Минимальная фактическая доля утечки для самоурона. */
    public float leakDamageThreshold = 0.1f;
    /** Урон за единицу утёкшей жидкости. */
    public float leakDamagePerUnit = 0.1f;

    public float leakScale = 0.8f;

    public IntegrityConduit(String name){
        super(name);
        botColor = Color.white;

        buildType = IntegrityConduitBuild::new;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{Core.atlas.find(name + "-bottom"), topRegions[0]};
    }

    public class IntegrityConduitBuild extends ConduitBuild {
        public final IntegrityLeakModule leak = new IntegrityLeakModule(this, leakThreshold, leakFraction, leakCurve, leakScale, leakDamages, leakDamageThreshold, leakDamagePerUnit);

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            super.handleLiquid(source, liquid, leak.handleSplit(liquid, amount));
        }

        public void damageSilent(float damage) {
            if (damage <= 0) return;
            float last = this.lastDamageTime;
            this.damage(damage);
            this.lastDamageTime = last;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            float dmg = leak.update();
            if (dmg > 0f) damageSilent(dmg);
        }
    }
}