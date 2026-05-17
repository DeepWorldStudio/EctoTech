package ectotech.world.blocks.pressure.interfaces;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import ectotech.EctoVars;

/** Pressure compressors interface */
public interface PressureGenerator extends Pressurized {

    float maxPressure();

    int pressureOutputSide();

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
    default float criticalPressure() {
        return Float.MAX_VALUE;
    }

    @Override
    default float superCriticalPressure() {
        return Float.MAX_VALUE;
    }

    @Override
    default boolean explodesOnSuperCritical() {
        return false;
    }

    @Override
    default float pressureEfficiency() {
        return 1.0f;
    }

    // Внешнее взаимодействие

    @Override
    default boolean isPressureInputSide(int side) {
        return false;
    }

    @Override
    default boolean isPressureOutputSide(int side) {
        return side == pressureOutputSide();
    }

    @Override
    default void applyExternalPressureChange(float pressureChange) {
        if (pressureChange < 0f) {
            self().noSleep();
            pressureModule().applyPressureChange(pressureChange / (self().block.size * self().block.size));
        }
    }

    // Инициация внешнего воздействия

    default void initiatePressureTransfer() {
    }

    // Обновление с учётом ограничений

    @Override
    default void updatePressure() {
        pressureModule().update(this);
        clampGeneratorPressure();
    }

    default boolean isPressureFull() {
        return pressure() >= maxPressure();
    }

    default void clampGeneratorPressure() {
        pressureModule().pressure = Mathf.clamp(pressure(), EctoVars.absMinPressure, maxPressure());
    }

    //UI

    @Override
    default String pressureBarText() {
        return Core.bundle.get("bar.ectotech-pressure", "Pressure");
    }

    @Override
    default float pressureBarFraction() {
        return Math.min(pressure() / maxPressure(), 1.0f);
    }

    @Override
    default Color pressureBarColor() {
        return EctoVars.defaultPressureBarColor;
    }

}