package ectotech.world.aspects;

import arc.graphics.Color;
import arc.math.Interp;
import ectotech.EctoVars;
import ectotech.world.blocks.production.BalancedCrafter;
import ectotech.world.blocks.production.BalancedCrafter.BalancedCrafterBuild;
import mindustry.ui.Bar;
import mindustry.world.meta.StatUnit;

/**
 * Подключает физику давления к BalancedCrafter. Без этого аспекта блок давлением не занимается.
 * Все параметры давления живут здесь, а не на блоке; кривая скидки — собственная (start/full/curve).
 */
public class PressureAspect extends BuildAspect {
    public float outflowTanhFactor = EctoVars.defaultTanhFactor;
    public float outflowExponentCoefficient = EctoVars.defaultExponentCoefficient;
    public float criticalPressure = Float.MAX_VALUE;
    public float superCriticalPressure = Float.MAX_VALUE;
    public float pressureFlow = 0f;
    public boolean explodesOnSuperCritical = false;

    public PressureAspect(float fullAtm, float powerReduction) {
        this(EctoVars.defaultPressure, fullAtm, powerReduction);
    }

    public PressureAspect(float startAtm, float fullAtm, float powerReduction){
        super(startAtm, fullAtm, powerReduction);
        curve = Interp.smooth;
    }

    @Override public String name(){ return "@bar.ectotech-pressure"; }
    @Override public StatUnit unit(){ return StatUnit.none; }
    @Override public Color color(){ return EctoVars.defaultPressureBarColor; }

    @Override
    public float value(BalancedCrafterBuild build){
        return build.pressure();
    }

    @Override
    public void updateOwner(BalancedCrafterBuild build){
        build.updatePressure();
        build.keepAwakeIfPressurized();
    }

    @Override
    public void attachTo(BalancedCrafter block){
        if(block.pressureAspect != null) throw new IllegalArgumentException(block.name + ": only one PressureAspect allowed");
        block.pressureAspect = this;
    }

    /** Тот же вид, что у остальных pressurized-блоков мода. */
    @Override
    public Bar createBar(BalancedCrafterBuild build){
        return new Bar(build::pressureBarText, build::pressureBarColor, build::pressureBarFraction);
    }
}