package ectotech.world.blocks.pressure.utils;

import mindustry.world.Block;

public class PressurizedConfigValidator {

    public static void validate(
            Block block,
            float operatingPressure,
            float thresholdPressure,
            boolean isPressureRequired,
            float minEfficiencyCoeff,
            float maxEfficiencyCoeff
    ) {
        String name = block.name;

        // 1. minEfficiencyCoeff in [0, 1]
        if (minEfficiencyCoeff < 0f || minEfficiencyCoeff > 1f) {
            throw new IllegalArgumentException(
                    "[Pressure] Block '" + name + "': minEfficiencyCoeff must be in [0, 1]. " +
                            "Received: " + minEfficiencyCoeff
            );
        }

        // 2. maxEfficiencyCoeff >= 1.0
        if (maxEfficiencyCoeff < 1f) {
            throw new IllegalArgumentException(
                    "[Pressure] Block '" + name + "': maxEfficiencyCoeff must be >= 1.0. " +
                            "Received: " + maxEfficiencyCoeff
            );
        }

        // 3. operatingPressure > 0
        if (operatingPressure <= 0f) {
            throw new IllegalArgumentException(
                    "[Pressure] Block '" + name + "': operatingPressure must be > 0. " +
                            "Received: " + operatingPressure
            );
        }

        // 3. thresholdPressure > 0
        if (thresholdPressure <= 0f) {
            throw new IllegalArgumentException(
                    "[Pressure] Block '" + name + "': thresholdPressure must be > 0. " +
                            "Received: " + thresholdPressure
            );
        }

        // 4. Главный инвариант при isPressureRequired = true
        if (isPressureRequired) {
            float opDist = Math.abs(operatingPressure - 1f);
            float thDist = Math.abs(thresholdPressure - 1f);

            // Должны быть по одну сторону от 1 атм
            if ((operatingPressure - 1f) * (thresholdPressure - 1f) < 0) {
                throw new IllegalArgumentException(
                        "[Pressure] Block '" + name + "': operatingPressure and thresholdPressure must be on the same side of 1 atm when isPressureRequired = true. " +
                                "Received: op = " + operatingPressure + ", th = " + thresholdPressure
                );
            }

            // operatingPressure должен быть дальше от 1, чем thresholdPressure
            if (opDist < thDist - 0.001f) {
                throw new IllegalArgumentException(
                        "[Pressure] Block '" + name + "': operatingPressure must be further from 1 atm than thresholdPressure when isPressureRequired = true. " +
                                "Received: op = " + operatingPressure + " (dist=" + opDist + "), th = " + thresholdPressure + " (dist=" + thDist + ")"
                );
            }
        }
        // 5. При isPressureRequired = false — проверок не требуется
    }
}