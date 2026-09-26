package ectotech.world.blocks.liquid;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import ectotech.world.modules.IntegrityLeakModule;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.blocks.liquid.LiquidJunction;

public class IntegrityLiquidJunction extends LiquidJunction {
    public TextureRegion bottomRegion;

    public float leakThreshold = 1f;
    public float leakFraction = 0.5f;
    public float leakCurve = 1f;

    public boolean leakDamages = false;
    public float leakDamageThreshold = 0.1f;
    public float leakDamagePerUnit = 0.1f;
    public float leakScale = 0.8f;

    public IntegrityLiquidJunction(String name){
        super(name);
        liquidCapacity = 100f;

        buildType = IntegrityLiquidJunctionBuild::new;
    }

    @Override
    public void load(){
        super.load();

        bottomRegion = Core.atlas.find(name + "-bottom");
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{bottomRegion, region};
    }

    public class IntegrityLiquidJunctionBuild extends LiquidJunctionBuild {
        public final IntegrityLeakModule leak = new IntegrityLeakModule(this, leakThreshold, leakFraction, leakCurve, leakScale, leakDamages, leakDamageThreshold, leakDamagePerUnit);

        @Override
        public void draw() {
            Draw.rect(bottomRegion, x, y);
            super.draw();
        }

        @Override
        public Building getLiquidDestination(Building source, Liquid liquid) {
            if (!leak.active()) return super.getLiquidDestination(source, liquid);
            return this;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            if(!leak.active()) return false;

            Building dest = super.getLiquidDestination(source, liquid);
            if (dest == null || dest == this || !dest.acceptLiquid(this, liquid)) return false;

            return dest.block.liquidCapacity - dest.liquids.get(liquid) > 0.0001f;
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount){
            if(amount <= 0f) return;

            Building dest = super.getLiquidDestination(source, liquid);

            float passable = 0f;

            if(dest != null && dest != this && dest.acceptLiquid(this, liquid)){
                float space = Math.max(dest.block.liquidCapacity - dest.liquids.get(liquid), 0f);
                passable = Math.min(amount, space);
            }

            float rejected = amount - passable;

            if (passable > 0.0001f) {
                float rest = leak.handleSplit(liquid, passable);

                if (rest > 0.0001f && dest.acceptLiquid(this, liquid)) {
                    dest.handleLiquid(this, liquid, rest);
                }
            }

            if (rejected > 0.0001f) {
                returnToSource(source, liquid, rejected);
            }
        }

        protected void returnToSource(Building source, Liquid liquid, float amount) {
            if (source == null || source.liquids == null || amount <= 0.0001f) return;
            source.liquids.add(liquid, amount);
        }

        public void damageSilent(float damage) {
            if (damage <= 0) return;
            float last = this.lastDamageTime;
            this.damage(damage);
            this.lastDamageTime = last;
        }

        @Override
        public void updateTile() {
            float dmg = leak.update();
            if (dmg > 0f) damageSilent(dmg);
        }
    }
}