package ectotech.world.blocks.production;

import arc.Core;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.EctoVars;
import ectotech.world.aspects.BuildAspect;
import ectotech.world.aspects.HeatAspect;
import ectotech.world.aspects.PressureAspect;
import ectotech.world.meta.EctoStatValues;
import ectotech.world.pressure.interfaces.Pressurized;
import ectotech.world.modules.PressureModule;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

/**
 * Крафтер с опциональными BuildAspect: каждый снижает запрос энергии и/или даёт boost к скорости
 * при достаточной обеспеченности ресурсом. Пустой aspects = обычный GenericCrafter с basePowerDraw.
 */
public class BalancedCrafter extends GenericCrafter {
    /** Запрос энергии за тик без скидок. Не вызывай consumePower() — он будет заменён. */
    public float basePowerDraw = 1000f / 60f;

    public Seq<BuildAspect> aspects = new Seq<>();

    /** Заполняются в init() из aspects; null, если аспекта нет. */
    public HeatAspect heatAspect;
    public PressureAspect pressureAspect;

    public BalancedCrafter(String name) {
        super(name);
        buildType = BalancedCrafterBuild::new;
    }

    @Override
    public void init() {
        if (!(basePowerDraw > 0f)) {
            throw new IllegalArgumentException(name + ": basePowerDraw must be > 0");
        }

        for (BuildAspect a : aspects) {
            a.validate(name);
            a.attachTo(this);
        }

        consumePowerDynamic(basePowerDraw, BalancedCrafterBuild::requestedPower);
        super.init();
    }

    public float maxBalanceEfficiency(){
        float e = 1f;
        for(BuildAspect a : aspects) e += a.maxBoost;
        return e;
    }

    @Override
    public void setStats(){
        super.setStats();
        if(aspects.isEmpty()) return;

        stats.add(Stat.maxEfficiency, maxBalanceEfficiency() * 100f, StatUnit.percent);
        stats.add(Stat.boostEffect, EctoStatValues.aspects(aspects));
    }

    @Override
    public void setBars(){
        super.setBars();
        if(aspects.isEmpty()) return;

        //заполнение — обеспеченность, как в ванилле; текст — текущий запрос
        removeBar("power");
        addBar("power", (BalancedCrafterBuild b) -> new Bar(
                () -> Core.bundle.format("bar.ectotech-powerdraw", Strings.autoFixed(b.requestedPower() * 60f * b.timeScale(), 1)),
                () -> Pal.powerBar,
                () -> b.power.status
        ));

        addBar("balance-efficiency", (BalancedCrafterBuild b) -> new Bar(
                () -> Core.bundle.format("bar.efficiency", Strings.autoFixed(b.efficiency * 100f, 1)),
                () -> Pal.accent,
                () -> Mathf.clamp(b.efficiency / maxBalanceEfficiency())
        ));

        for (int i = 0; i < aspects.size; i++) {
            BuildAspect a = aspects.get(i);
            if (!a.showBar) continue;
            addBar("aspect-" + i, a::createBar);
        }
    }

    public class BalancedCrafterBuild extends GenericCrafterBuild implements HeatConsumer, Pressurized {
        public float heat;
        public float[] sideHeat = new float[4];
        public PressureModule pressureModule = new PressureModule();

        protected float powerFraction = 1f, speedMultiplier = 1f;

        @Override public float[] sideHeat(){ return sideHeat; }
        @Override public float heatRequirement(){ return heatAspect == null ? 0f : heatAspect.full; }

        @Override public Building self(){ return this; }
        @Override public PressureModule pressureModule(){ return pressureModule; }
        @Override public float outflowTanhFactor(){ return pressureAspect == null ? EctoVars.defaultTanhFactor : pressureAspect.outflowTanhFactor; }
        @Override public float outflowExponentCoefficient(){ return pressureAspect == null ? EctoVars.defaultExponentCoefficient : pressureAspect.outflowExponentCoefficient; }
        @Override public float criticalPressure(){ return pressureAspect == null ? Float.MAX_VALUE : pressureAspect.criticalPressure; }
        @Override public float superCriticalPressure(){ return pressureAspect == null ? Float.MAX_VALUE : pressureAspect.superCriticalPressure; }
        @Override public float pressureFlow(){ return pressureAspect == null ? 0f : pressureAspect.pressureFlow; }
        @Override public boolean explodesOnSuperCritical(){ return pressureAspect != null && pressureAspect.explodesOnSuperCritical; }

        @Override
        public void updateConsumption() {
            for (BuildAspect a : aspects) a.updateOwner(this);

            float saving = 0f;
            speedMultiplier = 1f;
            for(BuildAspect a : aspects){
                float f = a.fraction(this);
                saving += a.saving(f);
                speedMultiplier += a.boost(f);
            }
            powerFraction = Mathf.clamp(1f - saving);

            super.updateConsumption();
        }

        @Override
        public float efficiencyScale(){
            return super.efficiencyScale() * speedMultiplier;
        }

        public float powerFraction() {
            return powerFraction;
        }

        /** Потенциальный запрос при текущих скидках, энергия/тик. */
        public float powerDraw() {
            return basePowerDraw * powerFraction;
        }

        /** Запрос работающей постройки; простой энергию не тянет. */
        public float requestedPower() {
            return enabled && shouldConsume() && shouldConsumePower ? powerDraw() : 0f;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(heat);
            writePressure(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            heat = read.f();
            readPressure(read, revision);
        }
    }
}