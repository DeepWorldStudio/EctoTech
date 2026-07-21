package ectotech.world.blocks.production;

import ectotech.world.pressure.interfaces.Pressurized;
import ectotech.world.pressure.utils.PressureModule;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.blocks.production.GenericCrafter;

public class PressurizedCrafter extends GenericCrafter {

    public float operatingPressure = 1f;
    public float thresholdPressure = 8f;

    public boolean isPressureRequired = true;

    public float minEfficiencyCoeff = 0f;
    public float maxEfficiencyCoeff = 1f;

    public float outflowTanhFactor = 20f;
    public float outflowExponentCoefficient = 1f;

    public float criticalPressure = Float.MAX_VALUE;
    public float superCriticalPressure = Float.MAX_VALUE;

    public float pressureFlow = 0f;

    public boolean explodesOnSuperCritical = false;

    public PressurizedCrafter(String name) {
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("pressure", (PressurizedCrafterBuild b) -> new Bar(
                b::pressureBarText,
                b::pressureBarColor,
                b::pressureBarFraction
        ));
    }

    public class PressurizedCrafterBuild extends GenericCrafterBuild implements Pressurized {

        @Override public Building self() { return this; }

        public PressureModule pressureModule = new PressureModule();

        @Override public PressureModule pressureModule() { return pressureModule; }

        @Override public float operatingPressure() { return operatingPressure; }
        @Override public float thresholdPressure() { return thresholdPressure; }
        @Override public boolean isPressureRequired() { return isPressureRequired; }
        @Override public float minEfficiencyCoeff() { return minEfficiencyCoeff; }
        @Override public float maxEfficiencyCoeff() { return maxEfficiencyCoeff; }

        @Override public float outflowTanhFactor() { return outflowTanhFactor; }
        @Override public float outflowExponentCoefficient() { return outflowExponentCoefficient; }

        @Override public float criticalPressure() { return criticalPressure; }
        @Override public float superCriticalPressure() { return superCriticalPressure; }

        @Override public float pressureFlow() { return pressureFlow; }
        @Override public boolean explodesOnSuperCritical() { return explodesOnSuperCritical; }

        @Override
        public void update() {
            updatePressure();
            super.update();
            keepAwakeIfPressurized();
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume()
                    && (!isPressureRequired() || pressureEfficiency() > 0f);
        }

        @Override
        public float efficiencyScale() {
            return super.efficiencyScale() * pressureEfficiency();
        }

        @Override
        public void write(arc.util.io.Writes write) {
            super.write(write);
            writePressure(write);
        }

        @Override
        public void read(arc.util.io.Reads read, byte revision) {
            super.read(read, revision);
            readPressure(read, revision);
        }
    }
}
