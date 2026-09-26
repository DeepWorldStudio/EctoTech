package ectotech.world.blocks.liquid;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Eachable;
import arc.util.Nullable;
import ectotech.world.modules.IntegrityLeakModule;
import mindustry.core.Renderer;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.distribution.DirectionLiquidBridge;

public class IntegrityLiquidBridge extends DirectionLiquidBridge {
    public TextureRegion bridgeConnectorRegion;

    public float leakThreshold = 1f;
    public float leakFraction = 0.5f;
    public float leakCurve = 1f;

    public boolean leakDamages = false;
    public float leakDamageThreshold = 0.1f;
    public float leakDamagePerUnit = 0.1f;
    public float leakScale = 0.8f;

    public IntegrityLiquidBridge(String name){
        super(name);

        buildType = IntegrityLiquidBridgeBuild::new;
    }

    @Override
    public void load(){
        super.load();

        bridgeConnectorRegion = Core.atlas.find(name + "-bridge-connector");
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        super.drawPlanRegion(plan, list);
    }

    @Override
    public void drawBridge(int rotation, float x1, float y1, float x2, float y2, @Nullable Color liquidColor) {
        super.drawBridge(rotation, x1, y1, x2, y2, liquidColor);

        Draw.alpha(Renderer.bridgeOpacity);

        if (bridgeConnectorRegion.found()) {
            Draw.rect(bridgeConnectorRegion, x1, y1, rotation * 90f);
            Draw.rect(bridgeConnectorRegion, x2, y2, ((rotation + 2) & 3) * 90f);
            Draw.reset();
        }
    }

    public class IntegrityLiquidBridgeBuild extends DuctBridgeBuild {
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