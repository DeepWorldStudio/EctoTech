package ectotech.world.blocks.distribution;

import arc.math.Mathf;
import ectotech.EctoVars;
import ectotech.world.pressure.interfaces.Pressurized;
import ectotech.world.pressure.interfaces.PressurizedNetworkMember;
import ectotech.world.pressure.utils.PressureModule;
import ectotech.world.pressure.utils.PressureNetworkTypes;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.blocks.distribution.Duct;

public class PneumaticDuct extends Duct {

    public float operatingPressure = 1f;
    public float thresholdPressure = 2.8f;

    public boolean isPressureRequired = false;

    public float minEfficiencyCoeff = 1f;
    public float maxEfficiencyCoeff = 1f;

    public float outflowTanhFactor = 1000f;
    public float outflowExponentCoefficient = 1f;

    public float criticalPressure = Float.MAX_VALUE;
    public float superCriticalPressure = Float.MAX_VALUE;

    public final float pressureFlow = 0f;

    public boolean explodesOnSuperCritical = true;

    public PneumaticDuct(String name) {
        super(name);

        sync = true;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("pressure", (PneumaticDuctBuild b) -> new Bar(
                b::pressureBarText,
                b::pressureBarColor,
                b::pressureBarFraction
        ));
    }

    public class PneumaticDuctBuild extends DuctBuild implements Pressurized, PressurizedNetworkMember {

        @Override
        public Building self() {
            return this;
        }

        public PressureModule pressureModule = new PressureModule();

        @Override
        public PressureModule pressureModule() {
            return pressureModule;
        }

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

        @Override public String pressureNetworkType() { return PressureNetworkTypes.PneumaticDuctsNetwork; }

        @Override
        public boolean canPressureOutputTo(Building target, int side) {
            // Всегда выводит только вперёд
            return side == rotation;
        }

        @Override
        public boolean canPressureInputFrom(Building source, int side) {
            if (!armored) return side != rotation;
            // else (armored)
            if (side == ((rotation + 2) & 3)) return true;
            if (side == rotation) return false;

            return isDuct && source.front() == this;
        }

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
        public void onProximityUpdate() {
            super.onProximityUpdate();
            rebuildNetwork();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            disconnectNetwork();
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