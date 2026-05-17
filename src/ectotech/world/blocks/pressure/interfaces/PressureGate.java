package ectotech.world.blocks.pressure.interfaces;

import arc.math.Mathf;
import ectotech.EctoVars;

public interface PressureGate extends Pressurized {

    int pressureInputSide();

    int pressureOutputSide();

    float pressureInputModifier();

    float getOutputAirFraction();

    boolean isOverflowGate();
    float overflowStartingModifier();

    //default-заглушки

    @Override
    default float operatingPressure() {
        return EctoVars.defaultPressure;
    }

    @Override
    default float thresholdPressure() {
        return EctoVars.defaultPressure;
    }

    @Override
    default boolean isPressureRequired() {
        return false;
    }

    @Override
    default float minEfficiencyCoeff() {
        return 1.0f;
    }

    @Override
    default float maxEfficiencyCoeff() {
        return 1.0f;
    }

    @Override
    default boolean explodesOnSuperCritical() {
        return false;
    }

    @Override
    default float pressureEfficiency() {
        return 1.0f;
    }

    // Модификация pressureFlowScale

    @Override
    default float pressureFlowScale() {
        if (pressure() <= EctoVars.defaultPressure) return 0f;
        return self().efficiency * getOutputAirFraction();
    }

    // Доступ к внешнему воздействию

    @Override
    default boolean isPressureInputSide(int side) {
        return side == pressureInputSide();
    }

    @Override
    default boolean isPressureOutputSide(int side) {
        return side == pressureOutputSide();
    }

    @Override
    default void applyExternalPressureChange(float pressureChange) {
        if (pressureChange > 0f) {
            self().noSleep();
            pressureModule().applyPressureChange(pressureChange / (self().block.size * self().block.size));
        }
    }

    // Инициация внешнего воздействия

    default void initiatePressureTransfer() {

    }

}
