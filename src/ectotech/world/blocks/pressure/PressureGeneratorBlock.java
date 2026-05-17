package ectotech.world.blocks.pressure;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.world.blocks.pressure.interfaces.PressureGate;
import ectotech.world.blocks.pressure.interfaces.PressureGenerator;
import ectotech.world.blocks.pressure.interfaces.Pressurized;
import ectotech.world.blocks.pressure.utils.PressureModule;
import ectotech.world.geometry.BlockContactGeometry;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.Block;

public class PressureGeneratorBlock extends Block {

    public float pressureFlow = 1f;

    public float maxPressure = 5f;

    public float outflowLinearFactor;
    public float outflowExponentCoefficient;

    public PressureGeneratorBlock(String name) {
        super(name);
        rotate = true;
        update = true;
        solid = true;
        destructible = true;
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("pressure", (PressureGeneratorBuild b) -> new Bar(
                b::pressureBarText,
                b::pressureBarColor,
                b::pressureBarFraction
        ));
    }

    public class PressureGeneratorBuild extends Building implements PressureGenerator {


        @Override
        public Building self() {
            return this;
        }

        public PressureModule pressureModule = new PressureModule();
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
        public float maxPressure() {
            return maxPressure;
        }

        @Override
        public float outflowLinearFactor() {
            return outflowLinearFactor;
        }

        @Override
        public float outflowExponentCoefficient() {
            return outflowExponentCoefficient;
        }

        @Override
        public int pressureOutputSide() {
            return rotation;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            outputConnections.rebuild(this, pressureOutputSide());
        }

        @Override
        public void updateTile() {
            // Сначала внутренняя генерация (с clamp)
            updatePressure();

            // Потом внешняя передача
            if (efficiency > 0) {
                initiatePressureTransfer(delta());
            }
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume() && !isPressureFull();
        }

        public void initiatePressureTransfer(float delta) {
            if (pressure() <= ectotech.EctoVars.absMinPressure) return;

            float availablePressure = pressure() - ectotech.EctoVars.absMinPressure;
            if (availablePressure <= 0f) return;

            float maxTransfer = Math.min(availablePressure, pressureFlow() * pressureFlowScale() * delta);

            if (outputConnections.airFraction >= 1f) return;

            for (int i = 0; i < outputConnections.ports.size; i++) {
                var port = outputConnections.ports.get(i);
                Building outputNeighbour = port.building();

                if (!(outputNeighbour instanceof Pressurized targetBuilding) || targetBuilding instanceof PressureGate || (targetBuilding instanceof PressureGenerator))
                    continue;

                float portTransfer = maxTransfer * port.fraction();
                if (portTransfer <= 0f) continue;

                targetBuilding.applyExternalPressureChange(portTransfer);
                this.applyExternalPressureChange(-portTransfer);
            }
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
