package ectotech.world.blocks.production;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.EctoVars;
import ectotech.world.modules.PressureModule;
import ectotech.world.pressure.interfaces.PressureConsumer;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.blocks.production.Pump;

public class PressurizedPump extends Pump {

    /** Давление, при котором насос имеет ровно 100% эффективности. */
    public float operatingPressure = 1f;

    /** Давление, при котором достигается minEfficiencyCoeff. Не порог отключения. */
    public float thresholdPressure = 2.8f;

    /** Если true, при отсутствии давления насос останавливается. */
    public boolean isPressureRequired = false;

    /** Эффективность при нулевом давлении. */
    public float minEfficiencyCoeff = 0.55f;

    /** Предельная эффективность */
    public float maxEfficiencyCoeff = 1.4f;

    public float outflowTanhFactor = EctoVars.defaultTanhFactor;
    public float outflowExponentCoefficient = EctoVars.defaultExponentCoefficient;

    public float criticalPressure = 3.2f;
    public float superCriticalPressure = 3.8f;

    /** Насос качает жидкость, но сам не создаёт давление в пневмосети. */
    public float pressureFlow = 0f;

    public boolean explodesOnSuperCritical = true;

    public PressurizedPump(String name){
        super(name);

        buildType = PressurizedPumpBuild::new;
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{bottomRegion, region};
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("pressure", (PressurizedPumpBuild b) -> new Bar(
                b::pressureBarText,
                b::pressureBarColor,
                b::pressureBarFraction
        ));
    }

    public class PressurizedPumpBuild extends PumpBuild implements PressureConsumer {

        public PressureModule pressureModule = new PressureModule();

        @Override public Building self(){ return this; }
        @Override public PressureModule pressureModule(){ return pressureModule; }

        @Override public float operatingPressure(){ return operatingPressure; }
        @Override public float thresholdPressure(){ return thresholdPressure; }
        @Override public boolean isPressureRequired(){ return isPressureRequired; }
        @Override public float minEfficiencyCoeff(){ return minEfficiencyCoeff; }
        @Override public float maxEfficiencyCoeff(){ return maxEfficiencyCoeff; }

        @Override public float outflowTanhFactor(){ return outflowTanhFactor; }
        @Override public float outflowExponentCoefficient(){ return outflowExponentCoefficient; }

        @Override public float criticalPressure(){ return criticalPressure; }
        @Override public float superCriticalPressure(){ return superCriticalPressure; }

        @Override public float pressureFlow(){ return pressureFlow; }
        @Override public boolean explodesOnSuperCritical(){ return explodesOnSuperCritical; }


        @Override
        public float pressureEfficiency() {
            float nominal = Math.max(operatingPressure(), 0.0001f);
            float current = Math.max(pressure(), 0f);

            if(current <= nominal){
                return Mathf.lerp(
                        Mathf.clamp(minEfficiencyCoeff(), 0f, 1f),
                        1f,
                        Mathf.clamp(current / nominal)
                );
            }

            float upper = Math.max(thresholdPressure(), nominal + 0.0001f);

            return Mathf.lerp(
                    1f,
                    Math.max(maxEfficiencyCoeff(), 1f),
                    Mathf.clamp((current - nominal) / (upper - nominal))
            );
        }

        @Override
        public boolean shouldConsume(){
            return super.shouldConsume()
                    && (!isPressureRequired() || pressure() > 0f);
        }

        @Override
        public float efficiencyScale(){
            return super.efficiencyScale() * pressureEfficiency();
        }

        @Override
        public void updateTile(){
            updatePressure();
            super.updateTile();
            keepAwakeIfPressurized();
        }

        @Override
        public void draw(){
            Draw.rect(bottomRegion, x, y);
            super.draw();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            writePressure(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            readPressure(read, revision);
        }
    }
}