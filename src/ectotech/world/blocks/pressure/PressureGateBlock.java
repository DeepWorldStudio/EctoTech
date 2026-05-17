package ectotech.world.blocks.pressure;

import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.EctoVars;
import ectotech.world.blocks.pressure.interfaces.PressureGate;
import ectotech.world.blocks.pressure.interfaces.PressureGenerator;
import ectotech.world.blocks.pressure.interfaces.Pressurized;
import ectotech.world.blocks.pressure.utils.PressureModule;
import ectotech.world.geometry.BlockContactGeometry;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.Block;
import arc.util.Time;

public class PressureGateBlock extends Block {

    /**
     * Изначально отрицательный
     */
    public float pressureFlow = -1f;
    public float pressureInputModifier = 0.82f;
    public float criticalPressure;
    public float superCriticalPressure;
    public float outflowLinearFactor;
    public float outflowExponentCoefficient;

    public boolean isOverflowGate = false;
    public float overflowStartingModifier = 0.9f;

    public PressureGateBlock(String name) {
        super(name);
        rotate = true;
        update = true;
        solid = true;
        destructible = true;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("pressure", (PressureGateBuild b) -> new Bar(
                b::pressureBarText,
                b::pressureBarColor,
                b::pressureBarFraction
        ));
    }

    public class PressureGateBuild extends Building implements PressureGate {

        @Override
        public Building self() {
            return this;
        }

        public PressureModule pressureModule = new PressureModule();
        public BlockContactGeometry inputConnections = new BlockContactGeometry();
        public BlockContactGeometry outputConnections = new BlockContactGeometry();

        @Override
        public PressureModule pressureModule() {
            return pressureModule;
        }

        @Override
        public float pressureFlow() {
            return pressureFlow;
        }

        @Override
        public float pressureInputModifier() {
            return pressureInputModifier;
        }

        @Override
        public float criticalPressure() {
            return criticalPressure;
        }

        @Override
        public float superCriticalPressure() {
            return superCriticalPressure;
        }

        @Override
        public float outflowLinearFactor() {
            return outflowLinearFactor;
        }

        @Override
        public float outflowExponentCoefficient() {
            return outflowExponentCoefficient;
        }

        // Геометрия
        @Override
        public int pressureInputSide() {
            return (rotation + 2) & 3;
        }

        @Override
        public int pressureOutputSide() {
            return rotation;
        }

        @Override
        public float getOutputAirFraction() {
            return outputConnections.airFraction;
        }

        @Override
        public boolean isOverflowGate() {
            return isOverflowGate;
        }

        @Override
        public float overflowStartingModifier() {
            return overflowStartingModifier;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            inputConnections.rebuild(this, pressureInputSide());
            outputConnections.rebuild(this, pressureOutputSide());
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume() && hasTargets();
        }

        @Override
        public void updateTile() {
            if (efficiency > 0) {
                initiatePressureTransfer(delta());
            }
            updatePressure();
        }

        public void initiatePressureTransfer(float delta) {
            if (inputConnections.airFraction > 0f) return;

            //pressureFlow изначально отрицательный
            float currentMaxInputFlow = Math.abs(pressureFlow()) * pressureInputModifier() * efficiency * delta;

            if (currentMaxInputFlow <= 0f) return;

            for (int i = 0; i < inputConnections.ports.size; i++) {
                var port = inputConnections.ports.get(i);
                Building inputNeighbour = port.building();

                if (!(inputNeighbour instanceof Pressurized targetBuilding) || targetBuilding instanceof PressureGate ||
                        (targetBuilding instanceof PressureGenerator gen && port.targetSide() != gen.pressureOutputSide()))
                    continue;

                float portRequest = currentMaxInputFlow * port.fraction();

                if (isOverflowGate()) {
                    portRequest *= calculateOverflowCoefficient(targetBuilding);
                }

                if (portRequest <= 0f) continue;

                float takenPressureAmount = Math.min(portRequest, Math.max(0f, targetBuilding.pressure() - ectotech.EctoVars.absMinPressure));

                if (takenPressureAmount > 0f) {
                    targetBuilding.applyExternalPressureChange(-takenPressureAmount);

                    this.applyExternalPressureChange(takenPressureAmount);
                }
            }

        }

        private float calculateOverflowCoefficient(Pressurized target) {
            float targetCrit = target.criticalPressure();
            if (targetCrit == Float.MAX_VALUE) return 0f;

            float targetPressure = target.pressure();
            PressureGateBlock gateBlock = (PressureGateBlock) block;

            float startCoefficient = targetCrit * gateBlock.overflowStartingModifier;

            if (targetPressure <= startCoefficient) return 0f;
            if (targetPressure >= targetCrit) return 1f;
            return (targetPressure - startCoefficient) / (targetCrit - startCoefficient);
        }


        private boolean hasTargets() {
            if (inputConnections.airFraction > 0f) return false;

            for (int i = 0; i < inputConnections.ports.size; i++) {
                var port = inputConnections.ports.get(i);
                Building neighbour = port.building();

                if (!(neighbour instanceof Pressurized target) ||
                        (target instanceof PressureGate) ||
                        (target instanceof PressureGenerator gen && port.targetSide() != gen.pressureOutputSide()))
                    continue;

                if (isOverflowGate() && calculateOverflowCoefficient(target) <= 0f) {
                    continue;
                }

                if (target.pressure() > EctoVars.absMinPressure) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            writePressure(write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            readPressure(read, revision);
        }
    }
}
