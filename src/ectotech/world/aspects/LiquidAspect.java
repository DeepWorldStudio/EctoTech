package ectotech.world.aspects;

import arc.graphics.Color;
import ectotech.world.blocks.production.BalancedCrafter;
import ectotech.world.blocks.production.BalancedCrafter.BalancedCrafterBuild;
import mindustry.type.Liquid;
import mindustry.world.meta.StatUnit;

/** По запасу жидкости в баке, не по потоку. */
public class LiquidAspect extends BuildAspect {
    public final Liquid liquid;

    public LiquidAspect(Liquid liquid, float full, float powerReduction) {
        this(liquid, 0f, full, powerReduction);
    }

    public LiquidAspect(Liquid liquid, float start, float full, float powerReduction) {
        super(start, full, powerReduction);
        this.liquid = liquid;
    }

    @Override public String name(){ return liquid.localizedName; }
    @Override public StatUnit unit(){ return StatUnit.liquidUnits; }
    @Override public Color color(){ return liquid.color; }

    @Override
    public float value(BalancedCrafterBuild build){
        return build.liquids == null ? 0f : build.liquids.get(liquid);
    }

    @Override
    public void attachTo(BalancedCrafter block){
        block.hasLiquids = true;
    }
}