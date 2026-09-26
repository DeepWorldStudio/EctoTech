package ectotech.world.blocks.liquid;

import ectotech.world.modules.IntegrityLeakModule;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.liquid.LiquidRouter;

public class IntegrityLiquidRouter extends LiquidRouter {
    public float leakThreshold = 1f;
    public float leakFraction = 0.5f;
    public float leakCurve = 1f;

    public boolean leakDamages = false;
    public float leakDamageThreshold = 0.1f;
    public float leakDamagePerUnit = 0.1f;
    public float leakScale = 0.8f;

    public IntegrityLiquidRouter(String name) {
        super(name);

        buildType = IntegrityLiquidRouterBuild::new;
    }

    public class IntegrityLiquidRouterBuild extends LiquidRouterBuild{
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