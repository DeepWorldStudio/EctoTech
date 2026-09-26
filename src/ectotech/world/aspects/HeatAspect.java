package ectotech.world.aspects;

import arc.Core;
import arc.graphics.Color;
import arc.math.Interp;
import ectotech.world.blocks.production.BalancedCrafter;
import ectotech.world.blocks.production.BalancedCrafter.BalancedCrafterBuild;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.meta.StatUnit;

public class HeatAspect extends BuildAspect {

    public HeatAspect(float full, float powerReduction){
        this(0f, full, powerReduction);
    }

    public HeatAspect(float start, float full, float powerReduction){
        super(start, full, powerReduction);
        curve = Interp.pow2Out;
    }

    @Override public String name(){ return "@bar.heatpercent"; }
    @Override public StatUnit unit(){ return StatUnit.heatUnits; }
    @Override public Color color(){ return Pal.lightOrange; }

    @Override
    public float value(BalancedCrafterBuild build){
        return build.heat;
    }

    @Override
    public void updateOwner(BalancedCrafterBuild build){
        build.heat = build.calculateHeat(build.sideHeat);
    }

    @Override
    public void attachTo(BalancedCrafter block){
        if(block.heatAspect != null) throw new IllegalArgumentException(block.name + ": only one HeatAspect allowed");
        block.heatAspect = this;
    }

    @Override
    public Bar createBar(BalancedCrafterBuild build) {
        return new Bar(
                () -> Core.bundle.format(name(), (int)(build.heat + 0.01f), (int)(fraction(build) * 100f + 0.01f)),
                this::color,
                () -> fraction(build)
        );
    }
}