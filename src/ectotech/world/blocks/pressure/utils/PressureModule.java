package ectotech.world.blocks.pressure.utils;

import arc.math.Mathf;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import ectotech.EctoVars;
import ectotech.world.blocks.pressure.interfaces.Pressurized;

public class PressureModule {

    public float pressure = EctoVars.defaultPressure;

    public float efficiency = 1.0f;

    public PressurizedNetwork network = null;

    public void update(Pressurized owner) {
        float outflow = outflowCalculate(owner);
        pressure -= outflow * Time.delta;

        pressure = Math.max(EctoVars.absMinPressure, pressure);

        efficiency = calculatePressureEfficiency(owner);

        float flow = owner.pressureFlow();
        float flowScale = owner.pressureFlowScale();
        if (flow != 0f && flowScale != 0f) {
            pressure += flow * flowScale * owner.self().delta();
            pressure = Math.max(EctoVars.absMinPressure, pressure);
        }

        if (network != null) {
            network.sync();
        }

        checkCritical(owner);
    }

    public float outflowCalculate(Pressurized owner) {
        float k = owner.outflowLinearFactor();
        float a = owner.outflowExponentCoefficient();

        return k * ((float) Math.exp(a * (pressure - EctoVars.defaultPressure)) - EctoVars.defaultPressure / pressure);
    }

    private void checkCritical(Pressurized owner) {
        float superCrit = owner.superCriticalPressure();
        float crit = owner.criticalPressure();

        if (pressure >= superCrit) {
            owner.onPressureSuperCritical();
            return;
        }

        if (pressure >= crit) {
            float damage = EctoVars.pressureCriticalDamageMultiplier * outflowCalculate(owner) * arc.util.Time.delta;

            owner.self().damage(damage);
            owner.onPressureCritical();
        }
    }

    public float calculatePressureEfficiency(Pressurized owner) {
        boolean isPressureRequired = owner.isPressureRequired();

        float operatingPressure = owner.operatingPressure();
        float thresholdPressure = owner.thresholdPressure();
        float minE = owner.minEfficiencyCoeff();
        float maxE = owner.maxEfficiencyCoeff();

        if (Mathf.equal(operatingPressure, EctoVars.defaultPressure) && Mathf.equal(thresholdPressure, EctoVars.defaultPressure))
            return 1.0f;

        float basis;
        if (!Mathf.equal(operatingPressure, thresholdPressure)) {
            basis = operatingPressure - thresholdPressure;
        } else {
            basis = operatingPressure - EctoVars.defaultPressure;
        }

        if (basis * (pressure - thresholdPressure) < 0 && !Mathf.equal(pressure, thresholdPressure)) {
            return isPressureRequired ? 0f : minE;
        }

        float k = (1.0f - minE) / basis;
        float calculatedEfficiency = 1.0f + k * (pressure - operatingPressure);

        return Math.min(calculatedEfficiency, maxE);
    }

    public void applyPressureChange(float pressureChange) {
        pressure += pressureChange;
        pressure = Math.max(EctoVars.absMinPressure, pressure);
    }

    void write(Writes write) {
        write.f(pressure);
        write.f(efficiency);
    }

    void read(Reads read, byte revision) {
        pressure = read.f();
        efficiency = read.f();
    }

}
